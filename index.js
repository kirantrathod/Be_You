
'use strict'
const functions = require('firebase-functions');
const admin =require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('/notifications/{userid}/{notification_id}').onWrite(event =>{
	
const userid=event.params.userid;
const notification_id=event.params.notification_id;
console.log('We have a notification to send to : ',userid)	;

	if(!event.data.val()){
		return console.log('A Notification has been deleted from database',notification_id);
	}
	const fromuser=admin.database().ref(`/notifications/${userid}/${notification_id}`).once('value');
	return fromuser.then(fromuserresult=>{
		const from_user_id=fromuserresult.val().from;
		console.log('You have a notification from :',from_user_id);
		const userQuery=admin.database().ref(`users/${from_user_id}/name`).once('value');
		return userQuery.then(userResult=>{
			
			const userName =userResult.val();
			
			const DeviceToken=admin.database().ref(`/users/${userid}/device_token`).once('value');
	return DeviceToken.then(result =>{
		const token_id=result.val();
		const payload={
		notification:{
		title :"Be_You",
        body :`${userName} has sent you request`,
        icon: "default",
		click_action :"com.example.kiran.be_you_notification"
		},
		data:{
			from_user_id:from_user_id
			
		}
	};
	
	return admin.messaging().sendToDevice(token_id, payload).then(response => 	{
		console.log('this was the notification feature');
	});
		
	});
		});
		
		
		
	});	
});

exports.sendMessageNotification = functions.database.ref('/messages_notifications/{userid}/{notification_id}').onWrite(event =>{
	
const userid=event.params.userid;
const notification_id=event.params.notification_id;
console.log('We have a notification to send to : ',userid)	;

	if(!event.data.val()){
		return console.log('A Notification has been deleted from database',notification_id);
	}
	const fromuser=admin.database().ref(`/messages_notifications/${userid}/${notification_id}`).once('value');
	return fromuser.then(fromuserresult=>{
		const from_user_id=fromuserresult.val().from;
		console.log('You have a notification from :',from_user_id);
		
		const message_from_user=fromuserresult.val().message;
		console.log('The message is :',message_from_user);
		
		const userQuery=admin.database().ref(`users/${from_user_id}/name`).once('value');
		return userQuery.then(userResult=>{
			const userName =userResult.val();
			
			const userQuery2=admin.database().ref(`users/${from_user_id}/thumb_image`).once('value');
			return userQuery2.then(userResult=>{
				const userprofile =userResult.val();
				
				const userQuery3=admin.database().ref(`users/${from_user_id}/status`).once('value');
				  return userQuery3.then(userResult=>{
					  const userstatus=userResult.val();
			
			const DeviceToken=admin.database().ref(`/users/${userid}/device_token`).once('value');
				return DeviceToken.then(result =>{
		const token_id=result.val();
		const payload={
		notification:{
		title :"Be_You",
        body :`${userName}: ${message_from_user}`,
        icon: "default",
		click_action :"com.example.kiran.be_you_message_message_notification"
		
		},
		data:{
			from_user_id:from_user_id,
			userName:userName,
			userprofile:userprofile,
			userstatus:userstatus
			
		}
	};
	
	return admin.messaging().sendToDevice(token_id, payload).then(response => 	{
		console.log('this was the notification feature');
	});
		
	});
	
	
	});
		
			});
		
	});	
});


});
