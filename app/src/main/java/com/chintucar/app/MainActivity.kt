package com.chintucar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.eclipse.paho.client.mqttv3.*
import java.util.UUID

private const val BROKER="tcp://broker.hivemq.com:1883"
private const val CONTROL="chintu-car/control"
private const val TELEMETRY="chintu-car/telemetry"

class MainActivity: ComponentActivity() {
 private var mqtt:MqttClient?=null
 override fun onCreate(b:Bundle?){
  super.onCreate(b)
  setContent {
   CarScreen(
    connect={cb->connect(cb)},
    send={cmd->publish(cmd)}
   )
  }
 }
 private fun connect(cb:(Boolean)->Unit){
  try{
   if(mqtt?.isConnected==true){ mqtt?.disconnect(); cb(false); return }
   mqtt=MqttClient(BROKER,"phone-"+UUID.randomUUID(),null)
   val o=MqttConnectOptions().apply{
    isCleanSession=true; connectionTimeout=10; keepAliveInterval=20; isAutomaticReconnect=true
   }
   mqtt!!.connect(o)
   mqtt!!.subscribe(TELEMETRY,0)
   mqtt!!.setCallback(object:MqttCallback{
    override fun connectionLost(t:Throwable?){runOnUiThread{cb(false)}}
    override fun messageArrived(t:String?,m:MqttMessage?){}
    override fun deliveryComplete(t:IMqttDeliveryToken?){}
   })
   cb(true)
  }catch(e:Exception){cb(false)}
 }
 private fun publish(s:String){
  try{if(mqtt?.isConnected==true)mqtt!!.publish(CONTROL,MqttMessage(s.toByteArray()))}catch(_:Exception){}
 }
}

@Composable
fun CarScreen(connect:((Boolean)->Unit)->Unit,send:(String)->Unit){
 var online by remember{mutableStateOf(false)}
 var speed by remember{mutableFloatStateOf(50f)}
 var actual by remember{mutableFloatStateOf(0f)}
 var command by remember{mutableStateOf("STOP")}
 fun go(c:String){
  command=c
  actual=if(c=="STOP"||c=="EMERGENCY_STOP")0f else speed/5f
  send(c)
 }
 MaterialTheme{
  Surface(Modifier.fillMaxSize(),Color(0xFFF5F7FB)){
   Column(Modifier.fillMaxSize().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally){
    Text("🚗 CHINTU CAR",fontSize=28.sp,fontWeight=FontWeight.Bold)
    Text(if(online)"🟢 CAR CONNECTED" else "🔴 CAR DISCONNECTED",fontWeight=FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    Card(Modifier.fillMaxWidth(),RoundedCornerShape(20.dp)){
     Column(Modifier.padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally){
      Text("REAL-TIME SPEED",fontSize=13.sp)
      Text("${"%.1f".format(actual)} km/h",fontSize=34.sp,fontWeight=FontWeight.Bold)
      Text("Command: $command",color=Color.Gray)
     }
    }
    Spacer(Modifier.height(12.dp))
    Button({go("FORWARD")},Modifier.size(150.dp,60.dp)){Icon(Icons.Default.KeyboardArrowUp,null);Text(" FORWARD")}
    Row(horizontalArrangement=Arrangement.Center,modifier=Modifier.fillMaxWidth()){
     Button({go("LEFT")},Modifier.size(125.dp,60.dp)){Icon(Icons.Default.KeyboardArrowLeft,null);Text(" LEFT")}
     Spacer(Modifier.width(12.dp))
     Button({go("RIGHT")},Modifier.size(125.dp,60.dp)){Icon(Icons.Default.KeyboardArrowRight,null);Text(" RIGHT")}
    }
    Button({go("BACKWARD")},Modifier.size(150.dp,60.dp)){Icon(Icons.Default.KeyboardArrowDown,null);Text(" BACKWARD")}
    Text("SPEED LIMIT: ${speed.toInt()}%")
    Slider(speed,{speed=it},valueRange=0f..100f)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
     Button({go("STOP")},Modifier.weight(1f)){Text("🛑 STOP")}
     Button({go("EMERGENCY_STOP")},Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFD32F2F))){Text("🚨 EMERGENCY")}
    }
    Spacer(Modifier.height(10.dp))
    Button({connect{online=it}},Modifier.fillMaxWidth()){Text(if(online)"DISCONNECT MQTT" else "CONNECT CAR")}
    Spacer(Modifier.height(10.dp))
    Card(Modifier.fillMaxWidth(),RoundedCornerShape(18.dp)){
     Column(Modifier.padding(14.dp)){
      Text("📍 LIVE LOCATION",fontWeight=FontWeight.Bold)
      Box(Modifier.fillMaxWidth().height(120.dp).background(Color(0xFFE4E9F1),RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center){
       Text("🗺️ GPS map will appear here")
      }
     }
    }
   }
  }
 }
}
