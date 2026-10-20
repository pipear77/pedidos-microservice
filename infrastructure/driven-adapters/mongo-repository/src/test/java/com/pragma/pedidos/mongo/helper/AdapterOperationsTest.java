package com.pragma.pedidos.mongo.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AdapterOperationsTest {

    @Mock
    private DummyMongoRepository repository;
    @Mock
    private ObjectMapper mapper;

    private DummyAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new DummyAdapter(repository, mapper, data -> new DummyEntity(data.getId()));
    }

    @Test
    void crudOperations_ShouldExecuteSuccessfully() {
        DummyEntity entity = new DummyEntity("1");
        DummyData data = new DummyData("1");

        when(mapper.map(entity, DummyData.class)).thenReturn(data);
        when(repository.save(data)).thenReturn(data);
        when(repository.saveAll(any())).thenReturn(List.of(data));
        when(repository.findById("1")).thenReturn(Optional.of(data));
        when(repository.findAll(any(Example.class))).thenReturn(List.of(data));
        when(repository.findAll()).thenReturn(List.of(data));

        assertNotNull(adapter.save(entity));
        assertEquals(1, adapter.saveAllEntities(List.of(entity)).size());
        assertNotNull(adapter.findById("1"));
        assertEquals(1, adapter.findByExample(entity).size());
        assertEquals(1, adapter.findAll().size());
    }

    static class DummyEntity {
        private final String id;
        public DummyEntity(String id) { this.id = id; }
        public String getId() { return id; }
    }

    static class DummyData {
        private final String id;
        public DummyData(String id) { this.id = id; }
        public String getId() { return id; }
    }

    interface DummyMongoRepository extends MongoRepository<DummyData, String>, QueryByExampleExecutor<DummyData> {}

    static class DummyAdapter extends AdapterOperations<DummyEntity, DummyData, String, DummyMongoRepository> {
        public DummyAdapter(DummyMongoRepository repository, ObjectMapper mapper, Function<DummyData, DummyEntity> toEntityFn) {
            super(repository, mapper, toEntityFn);
        }
    }
}