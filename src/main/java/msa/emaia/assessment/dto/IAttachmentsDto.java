package msa.emaia.assessment.dto;

public interface IAttachmentsDto {

    String getFilename();
    String getMimetype();
    String getOriginalName();
    String getFileId();
    Long getFilesize();
}