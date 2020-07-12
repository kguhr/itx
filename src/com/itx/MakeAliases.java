package com.itx;

// cryptojFIPS, ssljFIPS,certjFIPS, kindly include references to jcmFIPS and cryptojce jars inorder to avoid BCM

import java.util.Iterator;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.program.IProgramBase;
import com.crystaldecisions.sdk.plugin.desktop.user.IUser;
import com.crystaldecisions.sdk.plugin.desktop.user.IUserAlias;
import com.crystaldecisions.sdk.plugin.desktop.user.IUserAliases;


public class MakeAliases implements IProgramBase
{
	public static void main(String[] args)
	{
		if(args.length != 5)
		{
			System.out.println("5 parameters are needed: <CMSUserName> <CMSPassword> <CMSName> <GroupName> <NewEnterprisePassword>");
		} else
		{
			String username = args[0];
			String password = args[1];
			String cms = args[2];
			try
			{
				IEnterpriseSession enterpriseSession = CrystalEnterprise.getSessionMgr().logon(username, password, cms, "secEnterprise");
				IInfoStore infoStore = (IInfoStore) enterpriseSession.getService("InfoStore");
				new MakeAliases().run(enterpriseSession, infoStore, new String[] {args[3], args[4]});
			} catch (SDKException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void run(IEnterpriseSession enterpriseSession, IInfoStore infoStore, String[] args) throws SDKException
	{
		if(args.length != 2)
		{
			System.out.println("2 parameters are needed: <GroupName> <NewEnterprisePassword>");
		} else
		{
			String group = args[0];
			String enterprisePassword = args[1];
			IInfoObjects objects = infoStore.query("select * from ci_systemobjects where children(\"si_name = 'usergroup-user'\", \"si_name = '" + group + "'\")");
			for (Iterator iterator = objects.iterator(); iterator.hasNext();)
			{
				IUser object = (IUser) iterator.next();
				System.out.println("User: " + object.getTitle());
				IUserAliases aliases = object.getAliases();
				boolean hasEnterprise = false;
				for (Iterator iterator2 = aliases.iterator(); iterator2.hasNext();)
				{
					IUserAlias alias = (IUserAlias) iterator2.next();
					System.out.println("\tExisting Alias: " + alias.getAuthentication());
					if("secEnterprise".equals(alias.getAuthentication()))
					{
						hasEnterprise = true;
					}
				}
				if(!hasEnterprise)
				{
					System.out.println("\tAdding Alias : secEnterprise");
					object.getAliases().addNew("secEnterprise:" + object.getTitle(), false);
					object.setNewPassword(enterprisePassword);
				}
			}
			infoStore.commit(objects);
		}
	}
}
