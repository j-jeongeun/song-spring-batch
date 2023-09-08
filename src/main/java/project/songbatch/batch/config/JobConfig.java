package project.songbatch.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import project.songbatch.batch.tasklet.KumyoungSongApiTasklet;
import project.songbatch.batch.tasklet.TjSongApiTasklet;
import project.songbatch.song.service.SongService;

@Slf4j
@Configuration
public class JobConfig extends BatchAutoConfiguration {

    private final SongService songService;

    public JobConfig(SongService songService) {
        this.songService = songService;
    }

    @Bean
    public Job songJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("getSongApi", jobRepository)
            .start(getKumyoungSongApiStep(jobRepository, transactionManager))
            .next(getTjSongApiStep(jobRepository, transactionManager))
            .build();
    }

    @Bean
    public Step getKumyoungSongApiStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("KumyoungSongApiStep", jobRepository)
            .tasklet(new KumyoungSongApiTasklet(songService), platformTransactionManager)
            .build();
    }

    @Bean
    public Step getTjSongApiStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("TjSongApiStep", jobRepository)
            .tasklet(new TjSongApiTasklet(songService), platformTransactionManager)
            .build();
    }

}
