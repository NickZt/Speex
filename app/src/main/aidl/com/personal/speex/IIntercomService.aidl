// IIntercomService.aidl
package com.personal.speex;

// Declare any non-default types here with import statements
import com.personal.speex.IUserCallback;
import com.personal.speex.IntercomUserBean;
interface IIntercomService {
   void startRecord(int level,inout IntercomUserBean userBean);
       void stopRecord(int level,inout IntercomUserBean userBean);
       void leaveGroup();
       void registerCallback(IUserCallback callback);
       void unRegisterCallback(IUserCallback callback);
}
