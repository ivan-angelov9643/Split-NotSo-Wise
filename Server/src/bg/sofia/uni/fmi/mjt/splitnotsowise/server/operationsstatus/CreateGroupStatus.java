package bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus;

public enum CreateGroupStatus {
    SUCCESS,
    GROUP_NAME_ALREADY_EXISTS,
    MEMBERS_CONTAIN_CREATORS_USERNAME,
    MEMBER_DOES_NOT_EXIST
}
