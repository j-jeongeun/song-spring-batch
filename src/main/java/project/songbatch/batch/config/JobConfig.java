package project.songbatch.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import project.songbatch.common.domain.Song;
import project.songbatch.common.enums.BrandType;
import project.songbatch.song.service.ExternalService;
import project.songbatch.song.service.SongService;

@Configuration
@RequiredArgsConstructor
public class JobConfig extends BatchAutoConfiguration {

    private final ExternalService externalService;
    private final SongService songService;

    @Bean
    public Job syncSongJob(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new JobBuilder("syncSongJob", jobRepository)
            .start(getKumyoungSongChunk(jobRepository, transactionManager))
            .start(getTjSongChunk(jobRepository, transactionManager))
            .incrementer(new RunIdIncrementer())
            .build();
    }

    @Bean
    public Step getKumyoungSongChunk(
        JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("getKumyoungSongChunk", jobRepository)
            .chunk(50, transactionManager)
            .reader(songListReader(BrandType.KUMYOUNG))
            .processor(songListProcessor())
            .writer(songListWriter())
            .build();
    }

    @Bean
    public Step getTjSongChunk(
        JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("getTjSongChunk", jobRepository)
            .chunk(50, transactionManager)
            .reader(songListReader(BrandType.TJ))
            .processor(songListProcessor())
            .writer(songListWriter())
            .build();
    }

    protected ItemReader<Song> songListReader(BrandType brand) {
        return new ListItemReader<>(externalService.getSongListByReleaseDate(brand));
    }

    protected ItemProcessor<? super Object, ?> songListProcessor() {
        return item -> item;
    }

    protected ItemWriter<? super Object> songListWriter() {
        return items -> items.forEach((item) -> songService.create((Song) item));
    }

}
