package project.songbatch.batch.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import project.songbatch.common.domain.Song;
import project.songbatch.common.enums.BrandType;
import project.songbatch.song.service.SongService;
import project.songbatch.util.Util;

@Slf4j
@Component
@RequiredArgsConstructor
public class KumyoungSongApiTasklet implements Tasklet {

    private final SongService songService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws ParseException {
        ResponseEntity<String> getSongApi = Util.getSongListFromApi(BrandType.KUMYOUNG);

        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = (JSONArray) jsonParser.parse(getSongApi.getBody());

        log.info("getSongList("+BrandType.KUMYOUNG+") size: {}", jsonArray.size());

        int executionCnt = 0; // 새롭게 insert된 노래 수
        for (Object object : jsonArray) {
            JSONObject jsonObject = (JSONObject) object;
            BrandType brand = BrandType.valueOf(jsonObject.get("brand").toString().toUpperCase());
            Long no = Long.parseLong(jsonObject.get("no").toString());
            String title = jsonObject.get("title").toString();
            String singer = jsonObject.get("singer").toString();

            Song song = new Song(brand, no, title, singer);
            if(songService.findBySongId(song).isEmpty()) {
                songService.create(song);
                executionCnt++;
            }
        }

        log.info("new insert song("+BrandType.KUMYOUNG+") size: {}", executionCnt);

        return RepeatStatus.FINISHED;
    }

}
