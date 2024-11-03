package ru.gafarov.betservice.model;

public interface FriendInfo {

    Boolean getSubscribed();
    Double getTotalWinPercent();
    Double getTotalStandoffPercent();
    Integer getClosedBetCount();
    Double getWinPercent();
    Double getStandoffPercent();
    Integer getActiveBetCount();
    Integer getOfferedBetCount();
}
