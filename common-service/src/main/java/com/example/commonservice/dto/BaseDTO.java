package com.example.commonservice.dto;

import java.io.Serializable;

public interface BaseDTO extends Serializable {
    /**
     * Get the unique identifier of the DTO
     */
    Object getId();
}

