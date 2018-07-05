// IUserCallback.aidl
package com.personal.speex;

// Declare any non-default types here with import statements

interface IUserCallback {
   void findNewUser(String ipAddress);
       void removeUser(String ipAddress);
}
