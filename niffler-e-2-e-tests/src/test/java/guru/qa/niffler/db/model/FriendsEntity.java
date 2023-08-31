package guru.qa.niffler.db.model;

import java.util.Objects;

public class FriendsEntity {

    private UserDataEntity user;

    private UserDataEntity friend;

    private boolean pending;

    public UserDataEntity getUser() {
        return user;
    }

    public void setUser(UserDataEntity user) {
        this.user = user;
    }

    public UserDataEntity getFriend() {
        return friend;
    }

    public void setFriend(UserDataEntity friend) {
        this.friend = friend;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendsEntity that = (FriendsEntity) o;
        return pending == that.pending && Objects.equals(user, that.user) && Objects.equals(friend, that.friend);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, friend, pending);
    }
}