/*
 * Copyright 2015 David Nault and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnault.xmlpatchops4j;

public enum ErrorCondition {
	INVALID_ATTRIBUTE_VALUE, 
	INVALID_CHARACTER_SET, 
	INVALID_DIFF_FORMAT, 
	INVALID_ENTITY_DECLARATION, 
	INVALID_NAMESPACE_PREFIX,
	INVALID_NAMESPACE_URI, 
	INVALID_NODE_TYPES, 
	INVALID_PATCH_DIRECTIVE,
	INVALID_ROOT_ELEMENT_OPERATION, 
	INVALID_XML_PROLOG_OPERATION,
	INVALID_WHITESPACE_DIRECTIVE,
	UNLOCATED_NODE, 
	UNSUPORTED_ID_FUNCTION,
	UNSUPPORTED_XML_ID,
}
