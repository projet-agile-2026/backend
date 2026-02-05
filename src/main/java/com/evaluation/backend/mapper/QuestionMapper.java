package com.evaluation.backend.mapper;

import com.evaluation.backend.dto.QuestionDTO;
import com.evaluation.backend.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {QualificatifMapper.class})
public interface QuestionMapper {

    @Mapping(target = "qualificatif", source = "qualificatif")
    QuestionDTO toDTO(Question question);

    @Mapping(target = "qualificatif", ignore = true)
    Question toEntity(QuestionDTO questionDTO);

    List<QuestionDTO> toDTOList(List<Question> questions);
}