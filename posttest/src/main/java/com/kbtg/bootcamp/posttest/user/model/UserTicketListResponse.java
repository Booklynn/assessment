package com.kbtg.bootcamp.posttest.user.model;

import java.util.List;

public record UserTicketListResponse(List<String> tickets, int count, int cost) {
}
