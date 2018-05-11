package com.intuit.cg.backendtechassessment.data;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intuit.cg.backendtechassessment.config.AssessmentConfig;
import com.intuit.cg.backendtechassessment.data.entities.AutoBidEntity;
import com.intuit.cg.backendtechassessment.data.entities.BidEntity;
import com.intuit.cg.backendtechassessment.data.entities.ProjectEntity;
import com.intuit.cg.backendtechassessment.data.entities.UserEntity;
import lombok.Getter;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.ObjectRepository;

/**
 * So for that exercise, I originally wrote my own in-memory data store (using simple maps),
 * but it kind of sucked (because I need to query based on multiple keys), so I looked for a real in-memory data store.
 * Nitrate supports most of the things I wanted, like indexes and secondary indexes.
 * Their filtering API is also pretty nice so I decided to use it and re-wrote my data access layer.
 * The database is also locally persisted (path defined in config.yaml).
 * It wouldn't work for a real distributed project but it was fun to use.
 */
@Getter
@Singleton
public class InMemoryDataStore {
    private final ObjectRepository<ProjectEntity> projects;
    private final ObjectRepository<UserEntity> users;
    private final ObjectRepository<BidEntity> bids;
    private final ObjectRepository<AutoBidEntity> autoBids;

    @Inject
    public InMemoryDataStore(AssessmentConfig config) {
        Nitrite nitriteDb = Nitrite.builder()
                .compressed()
                .filePath(config.getDataStorePath())
                .openOrCreate();

        projects = nitriteDb.getRepository(ProjectEntity.class);
        users = nitriteDb.getRepository(UserEntity.class);
        bids = nitriteDb.getRepository(BidEntity.class);
        autoBids = nitriteDb.getRepository(AutoBidEntity.class);
    }
}
