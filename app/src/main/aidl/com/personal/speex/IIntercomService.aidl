// IIntercomService.aidl
package com.personal.speex;

// Declare any non-default types here with import statements
import com.personal.speex.IUserCallback;
interface IIntercomService {
   void startRecord();
       void stopRecord();
       void leaveGroup();
       void registerCallback(IUserCallback callback);
       void unRegisterCallback(IUserCallback callback);
}
