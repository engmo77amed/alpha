package com.fayroz.alpha.data

data class Client(val id:String, val name:String, val phone:String="")
data class Project(val id:String, val name:String, val clientId:String, val status:String="نشط", val contractType:String="تكلفة + إشراف", val base:Long=0, val extras:Long=0, val discounts:Long=0)
data class MoneyMove(val id:String, val projectId:String, val kind:String, val amount:Long, val date:String, val note:String="")
data class ImportedSummary(val clients:Int, val projects:Int, val payments:Int, val dues:Int, val expenses:Int, val warnings:List<String>)
