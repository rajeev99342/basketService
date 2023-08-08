SET FOREIGN_KEY_CHECKS=0;

SET @constraint_name = (SELECT CONSTRAINT_NAME
                        FROM information_schema.TABLE_CONSTRAINTS
                        WHERE CONSTRAINT_SCHEMA = 'store'
                        AND CONSTRAINT_NAME = 'onDeleteForeignKey');


SET @drop_statement = (select if(@constraint_name is NULL, 'select 1',CONCAT('ALTER TABLE store.order DROP FOREIGN KEY ',@constraint_name,';')));
PREPARE STMT FROM @drop_statement;
EXECUTE STMT;
DEALLOCATE PREPARE STMT;

ALTER TABLE store.order
    add constraint onDeleteForeignKey
        foreign key (user_id)
            references store.user (id)
            on delete cascade;


SET FOREIGN_KEY_CHECKS=1;