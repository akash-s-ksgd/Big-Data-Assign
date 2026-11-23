// Run with mongosh. Demonstrates flexible documents, CRUD and useful indexes.
db = db.getSiblingDB("ecommerce_demo");
db.products.drop();
db.products.insertMany([
 {sku:"P1001",name:"Wireless Mouse",category:"Electronics",brand:"Northstar",price:24.99,rating:4.4,stock:32,specs:{connectivity:"2.4GHz",color:"black"}},
 {sku:"P1002",name:"USB-C Hub",category:"Electronics",brand:"LinkPro",price:49.50,rating:4.6,stock:12,specs:{ports:7,power_delivery_w:100}},
 {sku:"P2001",name:"Trail Bottle",category:"Outdoors",brand:"Summit",price:18.00,rating:4.2,stock:0,specs:{capacity_ml:750,material:"steel"}},
 {sku:"P3001",name:"Desk Lamp",category:"Home",brand:"Luma",price:35.00,rating:4.1,stock:8,specs:{dimmable:true,color:"white"}}
]);
print("INSERT: catalog seeded with " + db.products.countDocuments() + " products");
db.products.createIndex({sku:1},{unique:true});
db.products.createIndex({category:1,price:1});
// category and price-range search
print("SEARCH: Electronics priced 20-50");
printjson(db.products.find({category:"Electronics",price:{$gte:20,$lte:50}},{_id:0,name:1,price:1}).toArray());
print("QUERY PLAN:");
printjson(db.products.find({category:"Electronics",price:{$gte:20,$lte:50}},{_id:0,name:1,price:1}).explain("executionStats").executionStats);
// stock update
print("UPDATE:");
printjson(db.products.updateOne({sku:"P1002"},{$inc:{stock:5}}));
printjson(db.products.findOne({sku:"P1002"},{_id:0,sku:1,name:1,stock:1}));
// remove discontinued item
print("DELETE:");
printjson(db.products.deleteOne({sku:"P2001"}));
print("REMAINING PRODUCTS:");
printjson(db.products.find({}, {_id:0}).sort({sku:1}).toArray());
