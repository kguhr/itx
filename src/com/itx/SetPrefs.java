package com.itx;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.program.IProgramBase;
import com.crystaldecisions.sdk.plugin.desktop.user.IUser;
import com.crystaldecisions.sdk.plugin.desktop.usergroup.IUserGroup;
import com.crystaldecisions.sdk.properties.IProperty;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import java.util.Iterator;
import java.util.Set;

public class SetPrefs implements IProgramBase {	
	public static void main(String[] args)
	{   
		if(args.length != 5)
		{
  	       System.out.println("5 parameters are needed: <CMSUserName> <CMSPassword> <CMSName> <sourceUserID> <Group>");
		} else
		{
			String username = args[0];
			String password = args[1];
			String cms = args[2];
		  try
			{	IEnterpriseSession enterpriseSession = CrystalEnterprise.getSessionMgr().logon(username, password, cms, "secEnterprise");
				IInfoStore infoStore = (IInfoStore) enterpriseSession.getService("InfoStore");
				
				new SetPrefs().run(enterpriseSession, infoStore, new String[] {args[3], args[4]});
			} catch (SDKException e)
			{
				e.printStackTrace();
			}
		}
}	
	
public void run(IEnterpriseSession es, IInfoStore iStore, String[] args) throws SDKException 
 {
	    String userToCopyFrom = args[0];
	    String groupName = args[1];
	    
	    IInfoObjects sourceUsers = null;
	    IInfoObject sourceUser = null;
	    IProperty sourceProp = null;
	    IInfoObjects oGroups = null;
	    IUserGroup oGroup = null;
	    IUser oUser = null;
	    IInfoObjects oUsers = null;
	    Set<String> destUsers = null;
	    Iterator userIterator = null;
	    
	    boolean cont = true;
	    boolean debug = false;
	    
	    if (cont) {
	      iStore = (IInfoStore)es.getService("", "InfoStore");
	      sourceUsers = iStore.query("SELECT SI_NAME, SI_DATA FROM CI_SYSTEMOBJECTS WHERE SI_PROGID = 'CrystalEnterprise.User' And SI_Name = '" + userToCopyFrom + "' And SI_DATA != NULL");
	      if (sourceUsers.getResultSize() != 0) {
	        sourceUser = (IInfoObject)sourceUsers.get(0);
	        sourceProp = sourceUser.properties().getProperty("SI_DATA");
	      } else {
	        System.out.println("There are no users by the name of " + userToCopyFrom);
	        cont = false;
	      } 
	      if (cont) {
	        oGroups = iStore.query("SELECT TOP 1 SI_NAME, SI_DATA, SI_GROUP_MEMBERS FROM CI_SYSTEMOBJECTS WHERE SI_KIND='UserGroup' AND SI_NAME='" + groupName + "'");
	        if (oGroups.getResultSize() > 0) {
	          oGroup = (IUserGroup)oGroups.get(0);
	        } else {
	          System.out.println("There were no groups with the name " + groupName + " in CMS");
	          cont = false;
	        } 
	        if (cont) {
	          destUsers = oGroup.getUsers();
	          
	        if (debug) {  
	        	System.out.println("destUsers= "+destUsers);
	            }
	        
	          userIterator = destUsers.iterator();
	          	
	          if (debug) {  
	        	  System.out.println("destUsers= "+destUsers.size());
		            }
	          
	          
	          if (destUsers.size() > 0) {
	            while (userIterator.hasNext()) {
	              
	              oUsers = iStore.query("SELECT TOP 1 SI_ID, SI_NAME, SI_DATA From CI_SYSTEMOBJECTS WHERE SI_KIND = 'User' And SI_ID = " + userIterator.next());
	          
	              if (debug) {  
	            	  System.out.println("oUsers: " + oUsers);
			            }
	                            
	              oUser = (IUser)oUsers.get(0);
	            
	              
	              oUser.properties().setProperty("SI_DATA", sourceProp.getValue());
	              iStore.commit(oUsers);
	            } 
	          } else {
	            System.out.println("There were no users found in the " + groupName + " group");
	          } 
	          System.out.println("Finished setting preferences");
	        } 
	      } 
	    } 
	    es.logoff();
	    es = null;
	  }
 }

