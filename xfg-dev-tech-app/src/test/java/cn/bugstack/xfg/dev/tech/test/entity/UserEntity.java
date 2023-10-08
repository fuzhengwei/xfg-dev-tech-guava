package cn.bugstack.xfg.dev.tech.test.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    private String userName;
    private String password;
    private Double amount;
    private Date createTime;

}
