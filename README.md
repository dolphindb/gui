# dolphindbgui

### 编译运行JavaGUI源码
1. IDEA编译：
IDEA打开JavaGUI项目以后点击IDEA左侧Maven按钮，
2. 命令行编译：
在maven官网下载maven-3.8.1的jar包并解压
配置环境变量
```shell
vi /etc/profile

#在最后加上这两句，并保存退出
MAVEN_HOME=/usr/local/apache-maven-3.8.1  #maven路径
export PATH=${MAVEN_HOME}/bin:${PATH}

source /etc/profile

#验证结果
mvn -version
```
配置资源保存位置
```shell
vi ../apache-maven-3.8.1/conf/settings.xml 

#修改或添加localRepository
<localRepository>/usr/local/MavenResources</localRepository>  #自定义路径
```
下载安装JDK
```shell
rpm -ivh jdk-11.0.3_linux-x64_bin.rpm

#配置环境变量
vim /etc/profile

#在最后加上这几句，并保存退出
export JAVA_HOME=/usr/java/default #（jdk所在目录）
export JRE_HOME=\$JAVA_HOME/
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar

source /etc/profile

#验证结果
java -version
```
编译JavaGUI的jar包
```shell
cd javagui
mvn package
```

3.运行编译好的GUI包
```shell
cd javagui
cd target

java -jar 2xdb-0.0.1-SNAPSHOT-assemble-all.jar -Dlook=cross com.xxdb.gui.XXDBMain
```