// IUserCallback.aidl
package com.personal.speex;

// Declare any non-default types here with import statements
import com.personal.speex.IntercomUserBean;
interface IUserCallback {
   void findNewUser(inout IntercomUserBean userBean);
       void removeUser(inout IntercomUserBean userBean);
}
