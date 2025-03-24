package com.grookage.leia.aerospike.client;

import com.aerospike.client.Bin;
import com.aerospike.client.IAerospikeClient;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.exp.Exp;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.Statement;
import com.grookage.leia.aerospike.storage.AerospikeRecord;
import com.grookage.leia.aerospike.storage.AerospikeStorageConstants;
import com.grookage.leia.models.schema.engine.SchemaState;
import com.grookage.leia.models.utils.MapperUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.print.attribute.standard.MediaSize;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class AerospikeManager {

    private final String namespace;
    private final AerospikeConfig aerospikeConfig;
    private final IAerospikeClient client;

    public AerospikeManager(AerospikeConfig aerospikeConfig) {
        this.namespace = aerospikeConfig.getNamespace();
        this.aerospikeConfig = aerospikeConfig;
        this.client = AerospikeClientUtils.getIClient(aerospikeConfig);
    }

    private Key getKey(
            final String recordKey
    ) {
        return new Key(namespace, AerospikeStorageConstants.SCHEMA_SET, recordKey);
    }

    private Record getRecord(
            final Key key,
            final String bin
    ) {
        return client.get(null, key, bin);
    }

    @SneakyThrows
    private Collection<Bin> getBins(final AerospikeRecord aerospikeRecord) {
        final var bins = new ArrayList<Bin>();
        bins.add(new Bin(AerospikeStorageConstants.DEFAULT_BIN, AerospikeClientUtils.compress(
                MapperUtils.mapper().writeValueAsBytes(aerospikeRecord)))
        );
        bins.add(new Bin(AerospikeStorageConstants.NAMESPACE_BIN, aerospikeRecord.getNamespace()));
        bins.add(new Bin(AerospikeStorageConstants.SCHEMA_STATE_BIN, aerospikeRecord.getSchemaState().name()));
        bins.add(new Bin(AerospikeStorageConstants.SCHEMA_BIN, aerospikeRecord.getSchemaName()));
        return bins;
    }

    public void save(AerospikeRecord aerospikeRecord) {
        final var key = getKey(aerospikeRecord.getReferenceId());
        final var bins = getBins(aerospikeRecord).toArray(Bin[]::new);
        final var writePolicy = new WritePolicy(client.getWritePolicyDefault());
        writePolicy.recordExistsAction = RecordExistsAction.UPDATE;
        writePolicy.sendKey = true;
        client.put(writePolicy, key, bins);
    }

    @SneakyThrows
    public Optional<AerospikeRecord> getRecord(final String key) {
        final var recordKey = getKey(key);
        final var storedRecord = getRecord(recordKey, AerospikeStorageConstants.DEFAULT_BIN);
        if (null == storedRecord) return Optional.empty();
        final var aerospikeRecord = storedRecord.getBytes(AerospikeStorageConstants.DEFAULT_BIN);
        if (null == aerospikeRecord) return Optional.empty();
        return Optional.ofNullable(MapperUtils.mapper().readValue(
                AerospikeClientUtils.retrieve(aerospikeRecord),
                AerospikeRecord.class)
        );
    }

    private void augmentExpressions(final String binName,
                                    final Set<String> comparators,
                                    final List<Exp> searchableExpressions) {
        final var expressions = comparators.stream().map(comparator -> Exp.eq(Exp.stringBin(binName), Exp.val(comparator)))
                .toList();
        if (!expressions.isEmpty()) {
            if (expressions.size() == 1) {
                searchableExpressions.add(expressions.get(0));
            } else {
                searchableExpressions.add(Exp.or(expressions.toArray(Exp[]::new)));
            }
        }
    }

    @SneakyThrows
    public List<AerospikeRecord> getRecords(final Set<String> namespaces,
                                            final Set<String> schemaNames,
                                            final Set<String> schemaStates) {

        final var queryStatement = new Statement();
        queryStatement.setNamespace(namespace);
        queryStatement.setBinNames(AerospikeStorageConstants.DEFAULT_BIN);
        queryStatement.setSetName(AerospikeStorageConstants.SCHEMA_SET);
        queryStatement.setMaxRecords(10000);
        final var queryPolicy = client.copyQueryPolicyDefault();
        final var searchableExpressions = new ArrayList<Exp>();
        augmentExpressions(AerospikeStorageConstants.NAMESPACE_BIN, namespaces, searchableExpressions);
        augmentExpressions(AerospikeStorageConstants.SCHEMA_BIN, schemaNames, searchableExpressions);
        augmentExpressions(AerospikeStorageConstants.SCHEMA_STATE_BIN, schemaStates, searchableExpressions);
        if (!searchableExpressions.isEmpty()) {
            if (searchableExpressions.size() == 1) {
                queryPolicy.setFilterExp(Exp.build(searchableExpressions.get(0)));
            } else {
                queryPolicy.setFilterExp(Exp.build(Exp.and(searchableExpressions.toArray(Exp[]::new))));
            }
        }
        final var aerospikeRecords = new ArrayList<AerospikeRecord>();
        try (final var rs = client.query(queryPolicy, queryStatement)) {
            while (rs.next()) {
                final var storageRecord = rs.getRecord();
                if (null != storageRecord) {
                    final var binRecord = storageRecord.getBytes(AerospikeStorageConstants.DEFAULT_BIN);
                    if (null != binRecord) {
                        aerospikeRecords.add(
                                MapperUtils.mapper().readValue(AerospikeClientUtils.retrieve(binRecord),
                                        AerospikeRecord.class)
                        );
                    }
                }
            }
        }
        return aerospikeRecords;
    }

    public boolean exists(final String configNamespace,
                          final String schemaName,
                          final String schemaState) {
        final var queryStatement = new Statement();
        queryStatement.setNamespace(namespace);
        queryStatement.setSetName(AerospikeStorageConstants.SCHEMA_SET);
        final var queryPolicy = client.copyQueryPolicyDefault();
        queryPolicy.filterExp = Exp.build(Exp.and(
                Exp.eq(Exp.stringBin(AerospikeStorageConstants.NAMESPACE_BIN), Exp.val(configNamespace)),
                Exp.eq(Exp.stringBin(AerospikeStorageConstants.SCHEMA_BIN), Exp.val(schemaName)),
                Exp.eq(Exp.stringBin(AerospikeStorageConstants.SCHEMA_STATE_BIN), Exp.val(schemaState))
        ));
        try (final var resultSet = client.query(queryPolicy, queryStatement)) {
            var resultCount = 0;
            while (resultSet.next()) {
                if (null != resultSet.getRecord()) {
                    resultCount++;
                }
            }
            return resultCount > 0;
        }
    }
}
