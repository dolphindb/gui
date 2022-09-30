package com.xxdb.gui.component;

import com.xxdb.DBConnection;
import com.xxdb.data.BasicStringVector;
import com.xxdb.data.BasicTable;
import com.xxdb.data.Vector;

import java.io.IOException;

public class XXDBGroup {
    private static DBConnection conn;
    private String name;
    private String[] users;
    private String[][] permissions; //0: name 1: status 2: allow 3: deny

    public static XXDBGroup[] initializeGroupList(DBConnection conn) {
        XXDBGroup.conn = conn;
        return updateGroupList();
    }

    public static XXDBGroup[] updateGroupList() {
        try {
            BasicTable data = (BasicTable) conn.run("getGroupAccess(getGroupList())");
            int rows = data.rows();
            XXDBGroup[] groups = new XXDBGroup[rows];
            Vector name = data.getColumn(0);
            Vector users = data.getColumn(1);
            for (int i = 0; i < rows; i++) {
                String[][] permissions = new String[9][4];
                for (int j = 0; j < 9; j++) {
                    permissions[j][0] = data.getColumnName(2 + j);
                    permissions[j][1] = data.getColumn(2 + j).get(i).getString().trim();
                    if (j < 3) {
                        permissions[j][2] = data.getColumn(11 + j * 2).get(i).getString().trim();
                        permissions[j][3] = data.getColumn(12 + j * 2).get(i).getString().trim();
                    } else if (j == 5 || j == 6)  {
                        permissions[j][2] = data.getColumn(7 + j * 2).get(i).getString().trim();
                        permissions[j][3] = data.getColumn(8 + j * 2).get(i).getString().trim();
                    }
                }
                String[] groupList = users.get(i).getString().trim().split(",");
                groups[i] = new XXDBGroup(name.get(i).getString(), groupList, permissions);
            }
            return groups;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private XXDBGroup(String name, String[] users, String[][] permissions) {
        this.name = name;
        this.users = users;
        this.permissions = permissions;
    }

    public void update() {
        updateUsers();
        updatePermissions();
    }

    protected boolean updateUsers() {
        try {
            BasicStringVector userVector = (BasicStringVector) conn.run("getUsersByGroupId(\""+ name + "\")");
            if (userVector == null) {
                users = null;
                return false;
            }
            String[] userList = new String[userVector.rows()];
            for (int i = 0; i < userVector.rows(); i++) {
                userList[i] = userVector.getString(i);
            }
            users = userList;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected boolean updatePermissions() {
        try {
            BasicTable data = (BasicTable) conn.run("getGroupAccess(\""+ name + "\")");
            if (data == null) {
                permissions = null;
                return false;
            }
            permissions = XXDBUser.tableToPermissions(data, 2);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public String[] getUsers() {
        return users;
    }

    public String[][] getPermissions() {
        return permissions;
    }

    @Override
    public String toString() {
        return name;
    }
}
