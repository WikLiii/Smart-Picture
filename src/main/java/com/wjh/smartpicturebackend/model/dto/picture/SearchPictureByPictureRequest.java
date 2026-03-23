package com.wjh.smartpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchPictureByPictureRequest implements Serializable {


    private Long pictureId;

    private static final long serialVersionUID = 1L;
}