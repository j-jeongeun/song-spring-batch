package project.songbatch.batch.config;

import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
@MapperScan(basePackages = "project.songbatch", sqlSessionFactoryRef = "songSqlSessionFactory")
public class SongDataSourceConfig {

    private final ApplicationContext applicationContext;
    private final String SONG_DATA_SOURCE = "songDataSource";

    public SongDataSourceConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean(SONG_DATA_SOURCE)
    @ConfigurationProperties(prefix = "spring.song-db.datasource.hikari")
    public DataSource songDataSource() {
        return DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean
    public SqlSessionFactoryBean songSqlSessionFactory(DataSource dataSource) throws IOException {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath:/mapper/*.xml"));
        factoryBean.setConfigLocation(
            applicationContext.getResource("classpath:mybatis-config.xml"));
        return factoryBean;
    }

    @Bean
    public SqlSessionTemplate songSqlSession(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    public DataSourceTransactionManager songTransactionManager(@Qualifier(SONG_DATA_SOURCE) DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}