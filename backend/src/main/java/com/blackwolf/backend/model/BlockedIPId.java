package com.blackwolf.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class BlockedIPId implements Serializable {
    private String ip;
    private String companyId;
}
