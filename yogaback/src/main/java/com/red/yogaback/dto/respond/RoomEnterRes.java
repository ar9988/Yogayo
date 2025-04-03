package com.red.yogaback.dto.respond;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoomEnterRes {
    private Long userId;
    private String userNickname;
}
