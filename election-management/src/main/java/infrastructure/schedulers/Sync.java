package infrastructure.schedulers;

import domain.Election;
import domain.annotations.Principal;
import infrastructure.repositories.RedisElectionRespository;
import infrastructure.repositories.SQLElectionRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class Sync {
    private final SQLElectionRepository sqlRepository;
    private final RedisElectionRespository redisRespository;

    public Sync(@Principal SQLElectionRepository sqlElectionRepository, RedisElectionRespository redisElectionRespository) {
        this.sqlRepository = sqlElectionRepository;
        this.redisRespository = redisElectionRespository;
    }

    @Scheduled(cron = "*/10 * * * * ?")
    void sync() {
        sqlRepository.findAll().forEach(election -> sqlRepository.sync(redisRespository.sync(election)));
    }
}
