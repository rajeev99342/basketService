SET FOREIGN_KEY_CHECKS=0;

SET @constraint_name = (SELECT CONSTRAINT_NAME
                        FROM information_schema.TABLE_CONSTRAINTS
                        WHERE CONSTRAINT_SCHEMA = 'store'
                        AND CONSTRAINT_NAME = 'FKhq1m50l0ke2fkqxxd6ubo3x4b');


SET @drop_statement = (select if(@constraint_name is NULL, 'select 1',CONCAT('ALTER TABLE store.cart_details DROP FOREIGN KEY ',@constraint_name,';')));
PREPARE STMT FROM @drop_statement;
EXECUTE STMT;
DEALLOCATE PREPARE STMT;

ALTER TABLE store.cart_details
    add constraint FKhq1m50l0ke2fkqxxd6ubo3x4b
        foreign key (cart_id)
            references store.cart (cart_id)
            on delete cascade;





SET @constraint_name = (SELECT CONSTRAINT_NAME
                        FROM information_schema.TABLE_CONSTRAINTS
                        WHERE CONSTRAINT_SCHEMA = 'store'
                        AND CONSTRAINT_NAME = 'FKcpl0mjoeqhxvgeeeq5piwpd3i');


SET @drop_statement = (select if(@constraint_name is NULL, 'select 1',CONCAT('ALTER TABLE store.order DROP FOREIGN KEY ',@constraint_name,';')));
PREPARE STMT FROM @drop_statement;
EXECUTE STMT;
DEALLOCATE PREPARE STMT;

ALTER TABLE store.order
    add constraint FKcpl0mjoeqhxvgeeeq5piwpd3i
        foreign key (user_id)
            references store.user (user_id)
            on delete cascade;




SET @constraint_name = (SELECT CONSTRAINT_NAME
                        FROM information_schema.TABLE_CONSTRAINTS
                        WHERE CONSTRAINT_SCHEMA = 'store'
                        AND CONSTRAINT_NAME = 'FKl70asp4l4w0jmbm1tqyofho4o');


SET @drop_statement = (select if(@constraint_name is NULL, 'select 1',CONCAT('ALTER TABLE store.cart DROP FOREIGN KEY ',@constraint_name,';')));
PREPARE STMT FROM @drop_statement;
EXECUTE STMT;
DEALLOCATE PREPARE STMT;

ALTER TABLE store.cart
    add constraint FKl70asp4l4w0jmbm1tqyofho4o
        foreign key (user_id)
            references store.user (user_id)
            on delete cascade;

SET @constraint_name = (SELECT CONSTRAINT_NAME
                        FROM information_schema.TABLE_CONSTRAINTS
                        WHERE CONSTRAINT_SCHEMA = 'store'
                        AND CONSTRAINT_NAME = 'FK4du41awqfrviisty6wc7caunp');


SET @drop_statement = (select if(@constraint_name is NULL, 'select 1',CONCAT('ALTER TABLE store.order_details DROP FOREIGN KEY ',@constraint_name,';')));
PREPARE STMT FROM @drop_statement;
EXECUTE STMT;
DEALLOCATE PREPARE STMT;

ALTER TABLE store.order_details
    add constraint FK4du41awqfrviisty6wc7caunp
        foreign key (order_id)
            references store.order (order_id)
            on delete cascade;



SET @constraint_name = (SELECT CONSTRAINT_NAME
                        FROM information_schema.TABLE_CONSTRAINTS
                        WHERE CONSTRAINT_SCHEMA = 'store'
                        AND CONSTRAINT_NAME = 'FKda8tuywtf0gb6sedwk7la1pgi');


SET @drop_statement = (select if(@constraint_name is NULL, 'select 1',CONCAT('ALTER TABLE store.address DROP FOREIGN KEY ',@constraint_name,';')));
PREPARE STMT FROM @drop_statement;
EXECUTE STMT;
DEALLOCATE PREPARE STMT;

ALTER TABLE store.address
    add constraint FKda8tuywtf0gb6sedwk7la1pgi
        foreign key (user_id)
            references store.user (user_id)
            on delete cascade;


SET FOREIGN_KEY_CHECKS=1;