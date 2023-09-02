package guru.qa.niffler.db.model;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

public class UserDataEntity {
    private UUID id;

    private String username;

    private CurrencyValues currency;

    private String firstname;

    private String surname;

    private byte[] photo;

    private List<FriendsEntity> friends = new ArrayList<>();

    private List<FriendsEntity> invites = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public CurrencyValues getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyValues currency) {
        this.currency = currency;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public @Nonnull List<FriendsEntity> getFriends() {
        return friends;
    }

    public void setFriends(List<FriendsEntity> friends) {
        this.friends = friends;
    }

    public @Nonnull List<FriendsEntity> getInvites() {
        return invites;
    }

    public void setInvites(List<FriendsEntity> invites) {
        this.invites = invites;
    }

    public void addFriends(boolean pending, UserDataEntity... friends) {
        List<FriendsEntity> friendsEntities = Stream.of(friends)
                .map(f -> {
                    FriendsEntity fe = new FriendsEntity();
                    fe.setUser(this);
                    fe.setFriend(f);
                    fe.setPending(pending);
                    return fe;
                }).toList();

        this.friends.addAll(friendsEntities);
    }

    public void removeFriends(UserDataEntity... friends) {
        for (UserDataEntity friend : friends) {
            getFriends().removeIf(f -> f.getFriend().getId().equals(friend.getId()));
        }
    }

    public void removeInvites(UserDataEntity... invitations) {
        for (UserDataEntity invite : invitations) {
            getInvites().removeIf(i -> i.getUser().getId().equals(invite.getId()));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDataEntity that = (UserDataEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(username, that.username) && currency == that.currency && Objects.equals(firstname, that.firstname) && Objects.equals(surname, that.surname) && Arrays.equals(photo, that.photo) && Objects.equals(friends, that.friends) && Objects.equals(invites, that.invites);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, username, currency, firstname, surname, friends, invites);
        result = 31 * result + Arrays.hashCode(photo);
        return result;
    }

    @Override
    public String toString() {
        return "UserDataEntity{" +
                "username=" + username +
                ", id='" + id + '\'' +
                '}';
    }
}
