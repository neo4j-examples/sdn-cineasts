/*
 * Copyright [2011-2016] "Neo Technology"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.neo4j.cineasts.converter;


import org.neo4j.cineasts.domain.User;
import org.neo4j.ogm.typeconversion.AttributeConverter;

public class UserRolesConverter implements AttributeConverter<User.SecurityRole[],String[]> {


    @Override
    public String[] toGraphProperty(User.SecurityRole[] value) {
        if(value==null) {
            return null;
        }
        String[] values = new String[(value.length)];
        int i=0;
        for(User.SecurityRole role : value) {
            values[i++]=role.name();
        }
        return values;
    }

    @Override
    public User.SecurityRole[] toEntityAttribute(String[] value) {
        User.SecurityRole[] roles =new User.SecurityRole[value.length];
        int i=0;
        for(String role : value) {
            roles[i++] = User.SecurityRole.valueOf(role);
        }
        return roles;
    }
}
