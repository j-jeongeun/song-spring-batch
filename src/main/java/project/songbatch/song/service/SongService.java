package project.songbatch.song.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.songbatch.common.domain.Song;
import project.songbatch.song.repository.SongRepository;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    public void create(Song song) {
        if (songRepository.findBySongId(song).isEmpty()) {
            songRepository.create(song);
        }
    }

}
