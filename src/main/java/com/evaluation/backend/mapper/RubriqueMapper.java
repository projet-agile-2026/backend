package com.evaluation.backend.mapper;

import com.evaluation.backend.dto.Rubrique.CreateRubriqueRequest;
import com.evaluation.backend.dto.Rubrique.RubriqueDTO;
import com.evaluation.backend.dto.Rubrique.UpdateRubriqueRequest;
import com.evaluation.backend.entity.Rubrique;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RubriqueMapper {

    @Mapping(target = "questions", ignore = true)
    RubriqueDTO toDTO(Rubrique rubrique);

    Rubrique toEntity(RubriqueDTO rubriqueDTO);

    @Mapping(target = "idRubrique", ignore = true)
    Rubrique toEntity(CreateRubriqueRequest request);

    @Mapping(target = "idRubrique", ignore = true)
    void updateEntityFromRequest(UpdateRubriqueRequest request, @MappingTarget Rubrique rubrique);

    List<RubriqueDTO> toDTOList(List<Rubrique> rubriques);
}