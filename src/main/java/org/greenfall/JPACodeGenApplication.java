package org.greenfall;

import lombok.extern.slf4j.Slf4j;
import org.greenfall.schema.parser.SQLParser;

public class JPACodeGenApplication {
    public static void main(String[] args){
        SQLParser.parse();
    }

}
