package com.pangyo255.gridviewexample;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class City extends RealmObject {
    // GSON을 사용하는 경우 필드 이름을 모호하게해서는 안됩니다.
    // proguard-rules.pro에 proguard 규칙을 추가하거나 @SerializedName 주석을 추가하십시오.
    @PrimaryKey
    private String name;

    private long votes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }
}