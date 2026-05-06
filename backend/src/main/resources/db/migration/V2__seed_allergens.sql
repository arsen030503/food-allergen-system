-- Initial allergen reference data (same catalog as legacy @PostConstruct bootstrap)

INSERT INTO allergens (name, standard, trigger_ingredients, severity, description) VALUES
('Gluten', 'EU', 'wheat,barley,rye,oats,spelt,kamut,flour,bread,pasta,semolina,wheat starch', 'HIGH', 'Cereals containing gluten'),
('Crustaceans', 'EU', 'shrimp,crab,lobster,prawn,crayfish,langoustine', 'HIGH', 'Crustacean shellfish'),
('Eggs', 'EU', 'egg,eggs,albumin,mayonnaise,meringue,lysozyme,ovalbumin', 'HIGH', 'Eggs and egg products'),
('Fish', 'EU', 'fish,cod,salmon,tuna,mackerel,anchovies,sardines,bass,flounder,haddock', 'HIGH', 'Fish and fish products'),
('Peanuts', 'EU', 'peanut,peanuts,groundnut,groundnuts,monkey nuts,peanut oil,peanut butter', 'HIGH', 'Peanuts and peanut products'),
('Soybeans', 'EU', 'soy,soya,soybean,tofu,tempeh,miso,edamame,soy sauce,tamari', 'HIGH', 'Soybeans and soy products'),
('Milk', 'EU', 'milk,dairy,cheese,butter,cream,yogurt,lactose,whey,casein,ghee', 'HIGH', 'Milk and dairy products'),
('Nuts', 'EU', 'almond,hazelnut,walnut,cashew,pecan,brazil nut,pistachio,macadamia,pine nut', 'HIGH', 'Tree nuts'),
('Celery', 'EU', 'celery,celeriac,celery seed,celery salt', 'MEDIUM', 'Celery and celeriac'),
('Mustard', 'EU', 'mustard,mustard seed,mustard oil,mustard leaves', 'MEDIUM', 'Mustard and mustard products'),
('Sesame', 'EU', 'sesame,sesame seed,tahini,sesame oil,til', 'HIGH', 'Sesame seeds and products'),
('Sulphites', 'EU', 'sulphite,sulfite,sulphur dioxide,sulfur dioxide,so2,e220,e221,e222,e223,e224', 'MEDIUM', 'Sulphur dioxide and sulphites'),
('Lupin', 'EU', 'lupin,lupine,lupin flour,lupin seed', 'MEDIUM', 'Lupin and lupin products'),
('Molluscs', 'EU', 'mollusc,mollusk,squid,octopus,clam,oyster,scallop,mussel,snail', 'HIGH', 'Molluscs and mollusc products'),
('Wheat', 'FDA', 'wheat,flour,bread,pasta,semolina,wheat starch,bulgur,farro', 'HIGH', 'Wheat and wheat products'),
('Shellfish', 'FDA', 'shrimp,crab,lobster,clam,oyster,scallop,mussel,squid', 'HIGH', 'Shellfish'),
('Tree Nuts', 'FDA', 'almond,cashew,walnut,pecan,pistachio,brazil nut,hazelnut,macadamia', 'HIGH', 'Tree nuts'),
('Sesame Seeds', 'FDA', 'sesame,tahini,sesame oil,sesame seed,til,gingelly', 'HIGH', 'Sesame seeds');
