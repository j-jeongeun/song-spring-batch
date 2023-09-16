package project.songbatch.song.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.songbatch.common.domain.Song;
import project.songbatch.common.enums.BrandType;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalService {

    @Value("${manual.parameters.release-date}")
    private String manualReleaseDate;

    public List<Song> getSongListByReleaseDate(BrandType brand) {
        ResponseEntity<String> getSongApi = new RestTemplate().exchange(
            getSearchUrl(brand),
            HttpMethod.GET,
            null,
            String.class
        );

        if (!getSongApi.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("외부 API 통신에 실패하였습니다.");
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Song> songList = objectMapper.readValue(getSongApi.getBody(), new TypeReference<>() {
            });

            return songList;
        } catch(JsonProcessingException e) {
            throw new IllegalStateException("JSON 데이터 변환에 실패하였습니다.");
        }
    }

    private String getSearchUrl(BrandType brand) {
        String releaseDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        releaseDate = manualReleaseDate.isEmpty() ? releaseDate : manualReleaseDate;

        log.info("ReleaseDate is {}", releaseDate);

        return "https://api.manana.kr/karaoke/release/" + releaseDate + "/" + brand + ".json";
    }

}
