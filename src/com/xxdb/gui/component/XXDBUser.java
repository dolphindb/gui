package com.xxdb.gui.component;

import com.xxdb.DBConnection;
import com.xxdb.data.BasicStringVector;
import com.xxdb.data.BasicTable;
import com.xxdb.data.Vector;

import java.io.IOException;

public class XXDBUser {
    public static DBConnection conn;
    private String username;
    private String type;
    private String[] groups;
    private String[][] permissions; //0: name 1: status 2: allow 3: deny

    public static XXDBUser[] initializeUserList(DBConnection conn) {
        XXDBUser.conn = conn;
        return updateUserList();
    }

    public static XXDBUser[] updateUserList() {
        try {
            BasicTable data = (BasicTable) conn.run("getUserAccess(getUserList())");
            int rows = data.rows();
            XXDBUser[] users = new XXDBUser[rows];
            Vector userid = data.getColumn(0);
            Vector groups = data.getColumn(1);
            Vector isAdmin = data.getColumn(2);
            for (int i = 0; i < rows; i++) {
                String[][] permissions = new String[9][4];
                for (int j = 0; j < 9; j++) {
                    permissions[j][0] = data.getColumnName(3 + j);
                    permissions[j][1] = data.getColumn(3 + j).get(i).getString().trim();
                    if (j < 3) {
                        permissions[j][2] = data.getColumn(12 + j * 2).get(i).getString().trim();
                        permissions[j][3] = data.getColumn(13 + j * 2).get(i).getString().trim();
                    } else if (j == 5 || j == 6) {
                        permissions[j][2] = data.getColumn(8 + j * 2).get(i).getString().trim();
                        permissions[j][3] = data.getColumn(9 + j * 2).get(i).getString().trim();
                    }
                }
                String[] groupList = groups.get(i).getString().trim().split(",");
                users[i] = new XXDBUser(userid.get(i).getString(), isAdmin.get(i).getString(), groupList, permissions);
            }
            return users;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private XXDBUser(String username, String isAdmin, String[] groups, String[][] permissions) {
        this.username = username;
        if (isAdmin.equalsIgnoreCase("true")) {
            this.type = "Administrator";
        } else {
            this.type = "Standard";
        }
        this.groups = groups;
        this.permissions = permissions;
    }


    public String getUsername() {
        return username;
    }

    public String getType() {
        return type;
    }

    public String[] getGroups() {
        return groups;
    }

    public String[][] getPermissions() {
        return permissions;
    }

    public void update() {
        updateGroups();
        updatePermissions();
    }

    protected boolean updateGroups() {
        try {
            BasicStringVector groupVector = (BasicStringVector) conn.run("getGroupsByUserId(\""+ username + "\")");
            if (groupVector == null) {
                groups = null;
                return false;
            }
            String[] groupList = new String[groupVector.rows()];
            for (int i = 0; i < groupVector.rows(); i++) {
                groupList[i] = groupVector.getString(i);
            }
            groups = groupList;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected boolean updatePermissions() {
        try {
            BasicTable data = (BasicTable) conn.run("getUserAccess(\""+ username + "\")");
            if (data == null) {
                permissions = null;
                return false;
            }
            permissions = tableToPermissions(data, 3);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    static String[][] tableToPermissions(BasicTable data, int offset) {
        String[][] permissions = new String[9][4];
        for (int j = 0; j < 9; j++) {
            permissions[j][0] = data.getColumnName(offset + j);
            permissions[j][1] = data.getColumn(offset + j).get(0).getString().trim();
            if (j < 3) {
                permissions[j][2] = data.getColumn(offset + 9 + j * 2).get(0).getString().trim();
                permissions[j][3] = data.getColumn(offset + 10 + j * 2).get(0).getString().trim();
            } else if (j == 5 || j == 6)  {
                permissions[j][2] = data.getColumn(offset + 5 + j * 2).get(0).getString().trim();
                permissions[j][3] = data.getColumn(offset + 6 + j * 2).get(0).getString().trim();
            }
        }
        return permissions;
    }

    @Override
    public String toString() {
        return username;
    }
}
