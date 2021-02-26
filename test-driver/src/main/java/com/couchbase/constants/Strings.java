package com.couchbase.constants;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mnunberg
 */
public class Strings {

  public final static String ADMIN_USER = "Administrator";
  public final static String PASSWORD = "password";
  public final static String DEFAULT_KEY = "Test";
  public final static String CONTENT_NAME= "content";
  public final static String INITIAL_CONTENT_VALUE= "initial";
  public final static String DEFAULT_CONTENT_VALUE= "default";
  public final static String UPDATED_CONTENT_VALUE= "updated";
  public final static String SUBDOC_CONTENT_NAME= "subDocContent";
  public final static String SUBDOC_DEFAULT_CONTENT_VALUE= "default_subDocContent";
  public final static String SUBDOC_UPDATED_CONTENT_VALUE= "updated_subDocContent";
  public static final String CONFIG_JSON_FILENAME = "ClusterConfig.json";


  //RBAC users names:
  //User with no  read/write on collection
  public final static String RBAC_USER_BA = "Bucketadmin";

  //User with read but no write on collection
  public final static String RBAC_USER_DR = "DataReaderOnly";

  //User with write but no read on collection
  public final static String RBAC_USER_DW = "DataWriterOnly";

  //User with both read and write on collection
  public final static String RBAC_USER_DR_DW = "DataReaderAndWriter";

  public final static String RBAC_USER_PASSWORD = "rbacpassword";


  //RBAC Roles:
  // Role with with no  read/write on collection
  public final static String RBAC_ROLE_BA = "bucket_admin";

  // Role with with read on collection
  public final static String RBAC_ROLE_DR = "data_reader";

  // Role with with write on collection
  public final static String RBAC_ROLE_DW = "data_writer";


  //CLI Login
  public final static String SSHPASSWORD = "couchbase";
  public final static String SSHUSERNAME = "root";
}

