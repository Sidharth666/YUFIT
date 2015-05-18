/*
 * Copyright (C) 2011 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

/**
 * Generates entities and DAOs for the example project DaoExample.
 * 
 * Run it as a Java application (not Android).
 * 
 * @author Markus
 */
public class ExampleDaoGenerator {

    private static final int APP_DB_VERSION = 1000;

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(APP_DB_VERSION, "com.maxwell.bodysensor.dao");

        addProfile(schema);
        addDevice(schema);
        addDailyRecord(schema);
        addHourlyRecord(schema);
        addMoveRecord(schema);
        addSleepLog(schema);
        addDailySleppScore(schema);

        new DaoGenerator().generateAll(schema, "app/src/main/java/");
    }

    private static void addProfile(Schema schema) {
        Entity entity = schema.addEntity(DBUtils.PROFILE.TABLE);
        entity.addIdProperty().primaryKey();
        entity.addStringProperty(DBUtils.PROFILE.COL_NAME).notNull();
        entity.addIntProperty(DBUtils.PROFILE.COL_GENDER).notNull();
        entity.addDateProperty(DBUtils.PROFILE.COL_BIRTHDAY).notNull();
        entity.addDoubleProperty(DBUtils.PROFILE.COL_HEIGHT).notNull();
        entity.addDoubleProperty(DBUtils.PROFILE.COL_WEIGHT).notNull();
        entity.addDoubleProperty(DBUtils.PROFILE.COL_STRIDE).notNull();
        entity.addByteArrayProperty(DBUtils.PROFILE.COL_PHOTO);
        entity.addIntProperty(DBUtils.PROFILE.COL_DAILY_GOAL).notNull();
        entity.addIntProperty(DBUtils.PROFILE.COL_SLEEP_LOG_BEGIN).notNull();
        entity.addIntProperty(DBUtils.PROFILE.COL_SLEEP_LOG_END).notNull();
        entity.addStringProperty(DBUtils.PROFILE.COL_TARGET_ADT_MAC);
        entity.addBooleanProperty(DBUtils.PROFILE.COL_IS_PRIMARY_PROFILE);
    }

    private static void addDevice(Schema schema) {
        Entity entity = schema.addEntity(DBUtils.DEVICE.TABLE);
        entity.addIdProperty().primaryKey();
        entity.addLongProperty(DBUtils.DEVICE.COL_PROFILE_ID);
        entity.addStringProperty(DBUtils.DEVICE.COL_MAC).notNull();
        entity.addIntProperty(DBUtils.DEVICE.COL_TYPE).notNull();
        entity.addStringProperty(DBUtils.DEVICE.COL_NAME);
        entity.addIntProperty(DBUtils.DEVICE.COL_BATTERY);
        entity.addDateProperty(DBUtils.DEVICE.COL_LAST_DAILY_SYNC);
        entity.addDateProperty(DBUtils.DEVICE.COL_LAST_HOURLY_SYNC);
        entity.addIntProperty(DBUtils.DEVICE.COL_TIMEZONE);
    }

    private static void addDailyRecord(Schema schema) {
        Entity entity = schema.addEntity(DBUtils.DailyRecord.TABLE);
        entity.addIdProperty().primaryKey();
        entity.addLongProperty(DBUtils.DailyRecord.COL_PROFILE_ID).notNull();;
        entity.addDateProperty(DBUtils.DailyRecord.COL_DATE);
        entity.addIntProperty(DBUtils.DailyRecord.COL_GOAL);
        entity.addIntProperty(DBUtils.DailyRecord.COL_PEDO);
        entity.addDoubleProperty(DBUtils.DailyRecord.COL_APP_ENERGY);
        entity.addDoubleProperty(DBUtils.DailyRecord.COL_DISTANCE);
        entity.addDoubleProperty(DBUtils.DailyRecord.COL_CALORIES);
    }

    private static void addHourlyRecord(Schema schema) {
        Entity entity = schema.addEntity(DBUtils.HourlyRecord.TABLE);
        entity.addIdProperty().primaryKey();
        entity.addLongProperty(DBUtils.HourlyRecord.COL_PROFILE_ID).notNull();;
        entity.addDateProperty(DBUtils.HourlyRecord.COL_DATE);
        entity.addIntProperty(DBUtils.HourlyRecord.COL_PEDO);
        entity.addDoubleProperty(DBUtils.HourlyRecord.COL_APP_ENERGY);
        entity.addDoubleProperty(DBUtils.HourlyRecord.COL_DISTANCE);
        entity.addDoubleProperty(DBUtils.HourlyRecord.COL_CALORIES);
    }

    private static void addMoveRecord(Schema schema) {
        Entity entity = schema.addEntity(DBUtils.MoveRecord.TABLE);
        entity.addIdProperty().primaryKey();
        entity.addLongProperty(DBUtils.MoveRecord.COL_PROFILE_ID).notNull();;
        entity.addDateProperty(DBUtils.MoveRecord.COL_DATE);
        entity.addIntProperty(DBUtils.MoveRecord.COL_MOVE);
    }

    private static void addSleepLog(Schema schema) {
        Entity entity = schema.addEntity(DBUtils.SleepLog.TABLE);
        entity.addIdProperty().primaryKey();
        entity.addLongProperty(DBUtils.SleepLog.COL_PROFILE_ID).notNull();;
        entity.addDateProperty(DBUtils.SleepLog.COL_DATE).notNull();;
        entity.addDateProperty(DBUtils.SleepLog.COL_START_TIME).notNull();;
        entity.addDateProperty(DBUtils.SleepLog.COL_STOP_TIME).notNull();;
    }

    private static void addDailySleppScore(Schema schema) {
        Entity entity = schema.addEntity(DBUtils.DailySleepScore.TABLE);
        entity.addIdProperty().primaryKey();
        entity.addLongProperty(DBUtils.DailySleepScore.COL_PROFILE_ID).notNull();;
        entity.addDateProperty(DBUtils.DailySleepScore.COL_DATE);
        entity.addDoubleProperty(DBUtils.DailySleepScore.COL_SCORE);
        entity.addIntProperty(DBUtils.DailySleepScore.COL_DURATION);
        entity.addIntProperty(DBUtils.DailySleepScore.COL_TIMESWOKE);
    }


    private static void addNote(Schema schema) {
        Entity note = schema.addEntity("Note");
        note.addIdProperty();
        note.addStringProperty("text").notNull();
        note.addStringProperty("comment");
        note.addDateProperty("date");
    }

    private static void addCustomerOrder(Schema schema) {
        Entity customer = schema.addEntity("Customer");
        customer.addIdProperty();
        customer.addStringProperty("name").notNull();

        Entity order = schema.addEntity("Order");
        order.setTableName("ORDERS"); // "ORDER" is a reserved keyword
        order.addIdProperty();
        Property orderDate = order.addDateProperty("date").getProperty();
        Property customerId = order.addLongProperty("customerId").notNull().getProperty();
        order.addToOne(customer, customerId);

        ToMany customerToOrders = customer.addToMany(order, customerId);
        customerToOrders.setName("orders");
        customerToOrders.orderAsc(orderDate);
    }

}
