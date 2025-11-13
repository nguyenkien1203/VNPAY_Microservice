package com.restaurant.data.model;

import java.io.Serializable;

public interface IBaseModel<I> extends Serializable {
    /**
     * Get the unique identifier of the DTO
     */
    I getId();
}

