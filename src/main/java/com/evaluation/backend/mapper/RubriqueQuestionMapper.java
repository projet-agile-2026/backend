package com.evaluation.backend.mapper;

import com.evaluation.backend.dto.RubriqueQuestionDTO;
import com.evaluation.backend.entity.RubriqueQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RubriqueQuestionMapper {

    RubriqueQuestionDTO toDTO(RubriqueQuestion rubriqueQuestion);
    @Mapping(target = "rubrique", ignore = true)
    @Mapping(target = "question", ignore = true)
    RubriqueQuestion toEntity(RubriqueQuestionDTO rubriqueQuestionDTO);

    List<RubriqueQuestionDTO> toDTOList(List<RubriqueQuestion> rubriqueQuestions);
}