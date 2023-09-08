package project.songbatch.batch.tasklet;

import java.util.ArrayList;
import java.util.List;
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
public class TjSongApiTasklet implements Tasklet {

    private final SongService songService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws ParseException {
        ResponseEntity<String> getSongApi = Util.getSongListFromApi(BrandType.tj);

        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = (JSONArray) jsonParser.parse(getSongApi.getBody());

        log.info("getSongList("+BrandType.kumyoung+") size: {}", jsonArray.size());

        List<Song> songList = new ArrayList<>();
        for (Object object : jsonArray) {
            JSONObject jsonObject = (JSONObject) object;
            BrandType brand = BrandType.valueOf(jsonObject.get("brand").toString());
            Long no = Long.parseLong(jsonObject.get("no").toString());
            String title = jsonObject.get("title").toString();
            String singer = jsonObject.get("singer").toString();

            Song song = new Song(brand, no, title, singer);
            songList.add(song);
        }

        songService.create(songList);

        return RepeatStatus.FINISHED;
    }

}
