package com.gridpadel.infrastructure.persistence.mapper;

import com.gridpadel.domain.model.Pair;
import com.gridpadel.domain.model.vo.PairId;
import com.gridpadel.domain.model.vo.PlayerName;
import com.gridpadel.infrastructure.persistence.dto.PairDto;
import org.springframework.stereotype.Component;

@Component
public class PairDtoMapper {

    public PairDto toDto(Pair pair) {
        if (pair.isBye()) {
            return new PairDto(pair.id().value(), null, null, true, null);
        }
        return new PairDto(
                pair.id().value(),
                pair.player1Name().value(),
                pair.player2Name().value(),
                false,
                pair.seed().getOrNull()
        );
    }

    public Pair fromDto(PairDto dto) {
        if (dto.bye()) {
            return Pair.restore(PairId.of(dto.id()), null, null, true, null);
        }
        return Pair.restore(
                PairId.of(dto.id()),
                PlayerName.of(dto.player1Name()),
                PlayerName.of(dto.player2Name()),
                false,
                dto.seed()
        );
    }
}
