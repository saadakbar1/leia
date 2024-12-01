package com.grookage.leia.common.stubs;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

@Data
@Builder
@Jacksonized
public class TestRawCollectionStub {
    List rawList;
    LinkedList rawLinkedList;
    Set rawSet;
    HashSet rawHashSet;
    Map rawMap;
    SortedMap rawSortedMap;
}
