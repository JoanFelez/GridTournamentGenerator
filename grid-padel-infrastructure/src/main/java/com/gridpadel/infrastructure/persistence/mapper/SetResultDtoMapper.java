package com.gridpadel.infrastructure.persistence.mapper;

import com.gridpadel.domain.model.vo.SetResult;
import com.gridpadel.infrastructure.persistence.dto.SetResultDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class SetResultDtoMapper {

    @Mapping(target = "pair1Games", source = "pair1Games")
    @Mapping(target = "pair2Games", source = "pair2Games")
    public abstract SetResultDto toDto(SetResult setResult);

    public SetResult fromDto(SetResultDto dto) {
        return SetResult.of(dto.pair1Games(), dto.pair2Games());
    }
}
