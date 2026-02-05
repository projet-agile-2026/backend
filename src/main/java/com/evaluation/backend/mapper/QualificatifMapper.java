package com.evaluation.backend.mapper;

import com.evaluation.backend.dto.QualificatifDTO;
import com.evaluation.backend.entity.Qualificatif;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface QualificatifMapper {

    QualificatifDTO toDTO(Qualificatif qualificatif);

    Qualificatif toEntity(QualificatifDTO qualificatifDTO);

    List<QualificatifDTO> toDTOList(List<Qualificatif> qualificatifs);
}