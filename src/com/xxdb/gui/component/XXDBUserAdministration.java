package com.xxdb.gui.component;

import com.xxdb.DBConnection;
import com.xxdb.data.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Enumeration;

public class XXDBUserAdministration extends JFrame {
    DBConnection conn;
    private final DefaultListModel<XXDBUser> userListModel;
    private final DefaultListModel<XXDBGroup> groupListModel;

    public XXDBUserAdministration(XXDBEditor editor) {
        super("User Administration");
        this.conn = editor.getConnection();
        userListModel = new DefaultListModel<>();
        groupListModel = new DefaultListModel<>();

        // user pane
        final JList<XXDBUser> userList = new JList<>(userListModel);
        XXDBUser[] users = XXDBUser.initializeUserList(conn);
        if (users == null) return;
        for (XXDBUser user: users) {
            userListModel.addElement(user);
        }
        JScrollPane userJsp = new JScrollPane(userList);
        JButton createUserBtn = new JButton("Create");

        JPanel userListPanel = new JPanel(new BorderLayout());
        userListPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        userListPanel.add(new JLabel("User Names"), BorderLayout.NORTH);
        userListPanel.add(userJsp, BorderLayout.CENTER);
        userListPanel.add(createUserBtn, BorderLayout.SOUTH);

        UserPanel userInfoPanel = new UserPanel();

        JPanel userPanel = new JPanel(new GridLayout(1, 2));
        userPanel.add(userListPanel);
        userPanel.add(userInfoPanel);


        // group Pane
        final JList<XXDBGroup> groupList = new JList<>(groupListModel);
        XXDBGroup[] groups = XXDBGroup.initializeGroupList(conn);
        if (groups == null) return;
        for (XXDBGroup group: groups) {
            groupListModel.addElement(group);
        }
        JScrollPane groupJsp = new JScrollPane(groupList);
        JButton createGroupBtn = new JButton("Create");

        JPanel groupListPanel = new JPanel(new BorderLayout());
        groupListPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        groupListPanel.add(new JLabel("Group Names"), BorderLayout.NORTH);
        groupListPanel.add(groupJsp, BorderLayout.CENTER);
        groupListPanel.add(createGroupBtn, BorderLayout.SOUTH);


        GroupMemberPanel groupInfoPanel = new GroupMemberPanel();
        JPanel groupPanel = new JPanel(new GridLayout(1, 2));
        groupPanel.add(groupListPanel);
        groupPanel.add(groupInfoPanel);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Users", userPanel);
        tabbedPane.addTab("Groups", groupPanel);

        this.add(tabbedPane);

        this.setSize(800, 550);
        this.setLocationRelativeTo(editor);
        this.setVisible(true);

        userList.addListSelectionListener(listSelectionEvent -> {
            int j = userList.getSelectedIndex();
            if (!userListModel.isEmpty() && j >= 0)
                userInfoPanel.showUserInfo(userListModel.getElementAt(j));
        });

        createUserBtn.addActionListener(actionEvent -> {
            JLabel usernameLbl = new JLabel("Username: ");
            JTextField usernameTxt = new JTextField(30);
            JPanel usernamePane = new JPanel();
            usernamePane.add(usernameLbl);
            usernamePane.add(usernameTxt);
            JLabel passwordLbl = new JLabel("Password: ");
            JTextField passwordTxt = new JTextField(30);
            JPanel passwordPane = new JPanel();
            passwordPane.add(passwordLbl);
            passwordPane.add(passwordTxt);
            JPanel createUserPane = new JPanel();
            createUserPane.setLayout(new BoxLayout(createUserPane, BoxLayout.Y_AXIS));
            createUserPane.add(usernamePane);
            createUserPane.add(passwordPane);
            int op = JOptionPane.showConfirmDialog(this, createUserPane, "Create new user", JOptionPane.DEFAULT_OPTION);
            if (op == JOptionPane.YES_OPTION) {
                try {
                    conn.run("createUser(\"" + usernameTxt.getText().trim() + "\", \"" + passwordTxt.getText().trim() + "\")");
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                refreshUserList();
            }
        });

        groupList.addListSelectionListener(listSelectionEvent -> {
            int j = groupList.getSelectedIndex();
            System.out.println("select " + j);
            if (!groupListModel.isEmpty() && j >= 0)
                groupInfoPanel.showGroupInfo(groupListModel.getElementAt(j));
        });

        createGroupBtn.addActionListener(actionEvent -> {
            JLabel nameLbl = new JLabel("Group Name: ");
            JTextField nameTxt = new JTextField(30);
            JPanel namePane = new JPanel();
            namePane.add(nameLbl);
            namePane.add(nameTxt);
            JLabel userLbl = new JLabel("Select users: ");
            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public Class<?> getColumnClass(int i) {
                    if (i == 0) return Boolean.class;
                    return super.getColumnClass(i);
                }
            };
            model.setColumnIdentifiers(new String[] {"Select", "User"});
            for (Enumeration<XXDBUser> usersList = userListModel.elements(); usersList.hasMoreElements();) {
                model.addRow(new Object[]{false, usersList.nextElement()});
            }
            JTable table = new JTable(model);
            table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JCheckBox()));
            JPanel userPane = new JPanel();
            userPane.add(userLbl);
            userPane.add(new JScrollPane(table));
            JPanel createGroupPane = new JPanel();
            createGroupPane.setLayout(new BoxLayout(createGroupPane, BoxLayout.Y_AXIS));
            createGroupPane.add(namePane);
            createGroupPane.add(userPane);
            int op = JOptionPane.showConfirmDialog(this, createGroupPane, "Create new user", JOptionPane.DEFAULT_OPTION);
            if (op == JOptionPane.YES_OPTION) {
                StringBuilder user = new StringBuilder();
                for (int i = 0; i < table.getRowCount(); i++) {
                    if ((boolean) table.getValueAt(i, 0)) {
                        System.out.println(table.getValueAt(i, 1));
                        user.append("`").append(table.getValueAt(i, 1));
                    }
                }
                try {
                    System.out.println("createGroup(\"" + nameTxt.getText().trim() + "\"," + user + ")");
                    conn.run("createGroup(\"" + nameTxt.getText().trim() + "\"," + user + ")");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Fail to create group: " + ex.getMessage());
                }
                refreshGroupList();
            }
        });
    }

    private void refreshUserList() {
        userListModel.clear();
        XXDBUser[] users = XXDBUser.updateUserList();
        if (users == null) return;
        for (XXDBUser user:users) {
            userListModel.addElement(user);
        }
    }

    private void refreshGroupList() {
        groupListModel.clear();
        XXDBGroup[] groups = XXDBGroup.updateGroupList();
        if (groups == null) return;
        for (XXDBGroup group:groups) {
            groupListModel.addElement(group);
        }
    }

    class UserPanel extends JPanel {
        private XXDBUser user;
        private JLabel username, userType;
        private DefaultListModel<String> userGroupsModel;
        private DefaultTableModel permitTableModel;
        private JButton addGroupBtn, refreshBtn;


        public UserPanel() {
            username = new JLabel("Username: ");
            userType = new JLabel("User Type: ");
            JLabel userGroups = new JLabel("User Groups: ");
            userGroupsModel = new DefaultListModel<>();
            JList<String> groupList = new JList<>(userGroupsModel);
            JScrollPane groupJsp = new JScrollPane(groupList);
            addGroupBtn = new JButton("Add Group");
            permitTableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int i, int i1) {
                    return false;
                }
            };
            String[] headers = new String[] {"Permission", "Type", "Allowed", "Denied"};
            permitTableModel.setColumnIdentifiers(headers);
            JTable permitTable = new JTable(permitTableModel);
            permitTable.setPreferredScrollableViewportSize(new Dimension(200, 150));
            JScrollPane permitTableJsp = new JScrollPane(permitTable);
            refreshBtn = new JButton("Refresh");

            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            this.add(username);
            username.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.add(userType);
            userType.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.add(userGroups);
            this.add(groupJsp);
            groupJsp.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.add(addGroupBtn);
            this.add(permitTableJsp);
            permitTableJsp.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.add(refreshBtn);

            addGroupBtn.setEnabled(false);
            refreshBtn.setEnabled(false);

            groupList.setCellRenderer(new DefaultListCellRenderer(){
                public void paintComponent(Graphics graphics)
                {
                    super.paintComponent(graphics);
                    graphics.setColor(Color.black);
                    graphics.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                }
            }) ;

            addGroupBtn.addActionListener(actionEvent -> {
                Object[] groupsAddList = null;
                try {
                    BasicStringVector vector = (BasicStringVector) conn.run("getGroupList()");
                    if (vector != null) {
                        groupsAddList = new Object[vector.rows()];
                        for (int i = 0; i < vector.rows(); i++) {
                            if (!vector.getString(i).equalsIgnoreCase("")) {
                                groupsAddList[i] = vector.getString(i);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String group = (String) JOptionPane.showInputDialog(this, "Choose a group: ", "group",
                        JOptionPane.PLAIN_MESSAGE, null, groupsAddList, "");
                if (group == null) return;
                if (group.equalsIgnoreCase("")) {
                    JOptionPane.showMessageDialog(this, "Add to group failed");
                    return;
                }
                try {
                    conn.run("addGroupMember(\""+ user.getUsername() + "\",\""+ group + "\")");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Add to group failed");
                    e.printStackTrace();
                    return;
                }
                if (user.updateGroups()) {
                    showUserInfo(user);
                }
            });

            JPopupMenu permitMnu = new JPopupMenu();
            JMenuItem permitEdit = new JMenuItem("Edit");
            permitMnu.add(permitEdit);

            permitTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        permitMnu.show(permitTable, mouseEvent.getX(), mouseEvent.getY());
                    }
                }
            });

            permitEdit.addActionListener(actionEvent -> {
                int selectedRow = permitTable.getSelectedRow();
                EditPermissionPane<XXDBUser> permissionPane = new EditPermissionPane<>(user.getPermissions()[selectedRow], conn, user);
                permissionPane.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent windowEvent) {
                        user.update();
                        showUserInfo(user);
                    }
                });
            });

            refreshBtn.addActionListener(actionEvent -> {
                user.update();
                showUserInfo(user);
            });


        }

        public void showUserInfo(XXDBUser user) {
            if (user == null) return;
            this.user = user;
            username.setText("Username: " + user.getUsername());
            userType.setText("User Type: " + user.getType());
            userGroupsModel.clear();
            for (String groupName: user.getGroups()) {
                userGroupsModel.addElement(groupName);
            }
            String[] headers = new String[] {"Permission", "Type", "Allowed", "Denied"};
            permitTableModel.setDataVector(user.getPermissions(), headers);
            addGroupBtn.setEnabled(true);
            refreshBtn.setEnabled(true);
        }

    }

    class GroupMemberPanel extends JPanel {
        private XXDBGroup group;
        private JLabel name;
        private DefaultListModel<String> groupUsersModel;
        private DefaultTableModel permitTableModel;
        private JButton addUserBtn, refreshBtn;

        public GroupMemberPanel() {
            name = new JLabel("Group Name: ");
            JLabel users = new JLabel("Group Users: ");
            groupUsersModel = new DefaultListModel<>();
            JList<String> groupList = new JList<>(groupUsersModel);
            JScrollPane groupJsp = new JScrollPane(groupList);
            addUserBtn = new JButton("Add User");
            permitTableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int i, int i1) {
                    return false;
                }
            };
            String[] headers = new String[] {"Permission", "Type", "Allowed", "Denied"};
            permitTableModel.setColumnIdentifiers(headers);
            JTable permitTable = new JTable(permitTableModel);
            permitTable.setPreferredScrollableViewportSize(new Dimension(200, 150));
            JScrollPane permitTableJsp = new JScrollPane(permitTable);
            refreshBtn = new JButton("Refresh");

            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            this.add(name);
            name.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.add(users);
            users.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.add(groupJsp);
            groupJsp.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.add(addUserBtn);
            this.add(permitTableJsp);
            permitTableJsp.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.add(refreshBtn);

            addUserBtn.setEnabled(false);
            refreshBtn.setEnabled(false);


            groupList.setCellRenderer(new DefaultListCellRenderer(){
                public void paintComponent(Graphics graphics)
                {
                    super.paintComponent(graphics);
                    graphics.setColor(Color.black);
                    graphics.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                }
            }) ;

            JPopupMenu permitMnu = new JPopupMenu();
            JMenuItem permitEdit = new JMenuItem("Edit");
            permitMnu.add(permitEdit);

            permitTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        permitMnu.show(permitTable, mouseEvent.getX(), mouseEvent.getY());
                    }
                }
            });

            permitEdit.addActionListener(actionEvent -> {
                int selectedRow = permitTable.getSelectedRow();
                EditPermissionPane<XXDBGroup> permissionPane = new EditPermissionPane<>(group.getPermissions()[selectedRow], conn, group);
                permissionPane.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent windowEvent) {
                        group.update();
                        showGroupInfo(group);
                    }
                });
            });

            refreshBtn.addActionListener(actionEvent -> {
                group.update();
                showGroupInfo(group);
            });


            addUserBtn.addActionListener(actionEvent -> {
                Object[] userAddList = null;
                try {
                    BasicStringVector vector = (BasicStringVector) conn.run("getUserList()");
                    if (vector != null) {
                        userAddList = new Object[vector.rows()];
                        for (int i = 0; i < vector.rows(); i++) {
                            if (!vector.getString(i).equalsIgnoreCase("")) {
                                userAddList[i] = vector.getString(i);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String user = (String) JOptionPane.showInputDialog(this, "Choose a user: ", "user",
                        JOptionPane.PLAIN_MESSAGE, null, userAddList, "");
                if (user == null) return;
                if (user.equalsIgnoreCase("")) {
                    JOptionPane.showMessageDialog(this, "Add to group failed");
                    return;
                }
                try {
                    conn.run("addGroupMember(\""+ user + "\",\""+ group.getName() + "\")");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Add to group failed");
                    e.printStackTrace();
                    return;
                }
                if (group.updateUsers()) {
                    showGroupInfo(group);
                }
            });
        }

        public void showGroupInfo(XXDBGroup group) {
            if (group == null) return;
            this.group = group;
            name.setText("Name: " + group.getName());
            groupUsersModel.clear();
            for (String userName: group.getUsers()) {
                groupUsersModel.addElement(userName);
            }
            String[] headers = new String[] {"Permission", "Type", "Allowed", "Denied"};
            permitTableModel.setDataVector(group.getPermissions(), headers);
            addUserBtn.setEnabled(true);
            refreshBtn.setEnabled(true);
        }
    }


}

class EditPermissionPane<T> extends JFrame {

    public EditPermissionPane(String[] permission, DBConnection conn, T obj) {
        String name;
        if (obj instanceof XXDBUser) {
            name = ((XXDBUser) obj).getUsername();
        } else {
            name = ((XXDBGroup) obj).getName();
        }
        JLabel label = new JLabel(permission[0]);
        JLabel statusLabel = new JLabel("Status: ");
        JComboBox<String> statusComboBox = new JComboBox<>(new String[] {"NONE", "ALLOW", "DENY"});
        JLabel allowLabel = new JLabel("Allowed: ");
        JTextField allowTxt = new JTextField(20);
        allowTxt.setText(permission[2]);
        JLabel denyLabel = new JLabel("Denied: ");
        JTextField denyTxt = new JTextField(20);
        denyTxt.setText(permission[3]);
        JPanel gridPane = new JPanel(new GridLayout(3, 2));
        gridPane.add(statusLabel);
        gridPane.add(statusComboBox);
        gridPane.add(allowLabel);
        gridPane.add(allowTxt);
        gridPane.add(denyLabel);
        gridPane.add(denyTxt);

        JButton okBtn = new JButton("Confirm");
        JButton cancelBtn = new JButton("Cancel");
        JPanel btnPane = new JPanel();
        btnPane.add(okBtn);
        btnPane.add(cancelBtn);

        Box vbox = Box.createVerticalBox();
        vbox.add(label);
        vbox.add(gridPane);
        vbox.add(btnPane);
        vbox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(vbox);
        this.setSize(200, 200);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        okBtn.addActionListener(actionEvent -> {
            String status = statusComboBox.getItemAt(statusComboBox.getSelectedIndex());
            String allow = allowTxt.getText().trim();
            String deny = denyTxt.getText().trim();
            try {
                conn.run("revoke(\"" + name + "\", " + permission[0] + ")");
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            if (status.equalsIgnoreCase("none")) {
                if (!allow.equalsIgnoreCase("")) {
                    try {
                        conn.run("grant(\"" + name + "\", " + permission[0] + ", \"" + allow + "\")");
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                } else if (!deny.equalsIgnoreCase("")) {
                    try {
                        conn.run("deny(\"" + name + "\", " + permission[0] + ", \"" + deny + "\")");
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    try {
                        conn.run("revoke(\"" + name + "\", " + permission[0] + ", \"*\")");
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            } else if (status.equalsIgnoreCase("allow")) {
                if (allow.equalsIgnoreCase("")) {
                    try {
                        conn.run("grant(\"" + name + "\", " + permission[0] + ", \"*\")");
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    try {
                        conn.run("grant(\"" + name + "\", " + permission[0] + ", \"" + allow + "\")");
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            } else {
                if (deny.equalsIgnoreCase("")) {
                    try {
                        conn.run("deny(\"" + name + "\", " + permission[0] + ", \"*\")");
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    try {
                        conn.run("deny(\"" + name + "\", " + permission[0] + ", \"" + deny + "\")");
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
            this.dispose();
        });

        cancelBtn.addActionListener(actionEvent -> this.dispose());
    }
}