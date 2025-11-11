package fun.aiboot.services;

import fun.aiboot.entity.*;
import fun.aiboot.mapper.*;
import fun.aiboot.services.impl.PermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionServiceTest {

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private UserToolMapper userToolMapper;

    @Mock
    private ToolMapper toolMapper;

    @Mock
    private RoleToolMapper roleToolMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void hasRole_EmptyRoleNameList_ReturnsTrue() {
        // Given
        String userId = "user1";
        List<String> roleNames = List.of();
        boolean requireAll = true;

        // When
        boolean result = permissionService.hasRoleName(userId, roleNames, requireAll);

        // Then
        assertTrue(result);
    }

    @Test
    void hasRole_NullRoleNameList_ReturnsTrue() {
        // Given
        String userId = "user1";
        List<String> roleNames = null;
        boolean requireAll = true;

        // When
        boolean result = permissionService.hasRoleName(userId, roleNames, requireAll);

        // Then
        assertTrue(result);
    }

    @Test
    void hasRole_RequireAllAndHasAllRoles_ReturnsTrueName() {
        // Given
        String userId = "user1";
        List<String> roleNames = List.of("admin", "user");
        boolean requireAll = true;

        // Mock user roles
        UserRole userRole1 = new UserRole();
        userRole1.setUserId(userId);
        userRole1.setRoleId("role1");

        UserRole userRole2 = new UserRole();
        userRole2.setUserId(userId);
        userRole2.setRoleId("role2");

        Role role1 = new Role();
        role1.setId("role1");
        role1.setName("admin");

        Role role2 = new Role();
        role2.setId("role2");
        role2.setName("user");

        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole1, userRole2));
        when(roleMapper.selectBatchIds(anyList())).thenReturn(List.of(role1, role2));

        // When
        boolean result = permissionService.hasRoleName(userId, roleNames, requireAll);

        // Then
        assertTrue(result);
        verify(userRoleMapper).selectList(any());
        verify(roleMapper).selectBatchIds(anyList());
    }

    @Test
    void hasRole_RequireAllButMissingRole_Name_ReturnsFalse() {
        // Given
        String userId = "user1";
        List<String> roleNames = List.of("admin", "user", "moderator");
        boolean requireAll = true;

        // Mock user roles
        UserRole userRole1 = new UserRole();
        userRole1.setUserId(userId);
        userRole1.setRoleId("role1");

        UserRole userRole2 = new UserRole();
        userRole2.setUserId(userId);
        userRole2.setRoleId("role2");

        Role role1 = new Role();
        role1.setId("role1");
        role1.setName("admin");

        Role role2 = new Role();
        role2.setId("role2");
        role2.setName("user");

        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole1, userRole2));
        when(roleMapper.selectBatchIds(anyList())).thenReturn(List.of(role1, role2));

        // When
        boolean result = permissionService.hasRoleName(userId, roleNames, requireAll);

        // Then
        assertFalse(result);
    }

    @Test
    void hasRole_NotRequireAllAndHasOneRole_Name_ReturnsTrue() {
        // Given
        String userId = "user1";
        List<String> roleNames = List.of("admin", "moderator");
        boolean requireAll = false;

        // Mock user roles
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId("role1");

        Role role = new Role();
        role.setId("role1");
        role.setName("admin");

        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(roleMapper.selectBatchIds(anyList())).thenReturn(List.of(role));

        // When
        boolean result = permissionService.hasRoleName(userId, roleNames, requireAll);

        // Then
        assertTrue(result);
    }

    @Test
    void hasRole_NotRequireAllAndHasNoMatchingRole_Name_ReturnsFalse() {
        // Given
        String userId = "user1";
        List<String> roleNames = List.of("moderator", "superuser");
        boolean requireAll = false;

        // Mock user roles
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId("role1");

        Role role = new Role();
        role.setId("role1");
        role.setName("admin");

        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(roleMapper.selectBatchIds(anyList())).thenReturn(List.of(role));

        // When
        boolean result = permissionService.hasRoleName(userId, roleNames, requireAll);

        // Then
        assertFalse(result);
    }

    @Test
    void hasTool_EmptyToolNameList_ReturnsTrue() {
        // Given
        String userId = "user1";
        List<String> toolNames = List.of();
        boolean requireAll = true;

        // When
        boolean result = permissionService.hasToolIds(userId, toolNames, requireAll);

        // Then
        assertTrue(result);
    }

    @Test
    void hasTool_NullToolNameList_ReturnsTrue() {
        // Given
        String userId = "user1";
        List<String> toolNames = null;
        boolean requireAll = true;

        // When
        boolean result = permissionService.hasToolIds(userId, toolNames, requireAll);

        // Then
        assertTrue(result);
    }

    @Test
    void hasTool_RequireAllAndHasAllTools_ReturnsTrueName() {
        // Given
        String userId = "user1";
        List<String> toolNames = List.of("tool1", "tool2");
        boolean requireAll = true;

        // Mock user impl (direct and through roles)
        UserTool userTool = new UserTool();
        userTool.setUserId(userId);
        userTool.setToolId("tool1");

        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId("role1");

        RoleTool roleTool = new RoleTool();
        roleTool.setRoleId("role1");
        roleTool.setToolId("tool2");

        Tool tool1 = new Tool();
        tool1.setId("tool1");
        tool1.setName("tool1");

        Tool tool2 = new Tool();
        tool2.setId("tool2");
        tool2.setName("tool2");

        when(userToolMapper.selectList(any())).thenReturn(List.of(userTool));
        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(roleToolMapper.selectList(any())).thenReturn(List.of(roleTool));
        when(toolMapper.selectBatchIds(anyList())).thenReturn(List.of(tool1, tool2));

        // When
        boolean result = permissionService.hasToolIds(userId, toolNames, requireAll);

        // Then
        assertTrue(result);
        verify(userToolMapper).selectList(any());
        verify(userRoleMapper).selectList(any());
        verify(roleToolMapper).selectList(any());
        verify(toolMapper).selectBatchIds(anyList());
    }

    @Test
    void hasTool_RequireAllButMissingTool_Name_ReturnsFalse() {
        // Given
        String userId = "user1";
        List<String> toolNames = List.of("tool1", "tool2", "tool3");
        boolean requireAll = true;

        // Mock user impl (direct and through roles)
        UserTool userTool = new UserTool();
        userTool.setUserId(userId);
        userTool.setToolId("tool1");

        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId("role1");

        RoleTool roleTool = new RoleTool();
        roleTool.setRoleId("role1");
        roleTool.setToolId("tool2");

        Tool tool1 = new Tool();
        tool1.setId("tool1");
        tool1.setName("tool1");

        Tool tool2 = new Tool();
        tool2.setId("tool2");
        tool2.setName("tool2");

        when(userToolMapper.selectList(any())).thenReturn(List.of(userTool));
        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(roleToolMapper.selectList(any())).thenReturn(List.of(roleTool));
        when(toolMapper.selectBatchIds(anyList())).thenReturn(List.of(tool1, tool2));

        // When
        boolean result = permissionService.hasToolIds(userId, toolNames, requireAll);

        // Then
        assertFalse(result);
    }

    @Test
    void hasTool_NotRequireAllAndHasOneTool_Name_ReturnsTrue() {
        // Given
        String userId = "user1";
        List<String> toolNames = List.of("tool2", "tool3");
        boolean requireAll = false;

        // Mock user impl (direct and through roles)
        UserTool userTool = new UserTool();
        userTool.setUserId(userId);
        userTool.setToolId("tool1");

        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId("role1");

        RoleTool roleTool = new RoleTool();
        roleTool.setRoleId("role1");
        roleTool.setToolId("tool2");

        Tool tool1 = new Tool();
        tool1.setId("tool1");
        tool1.setName("tool1");

        Tool tool2 = new Tool();
        tool2.setId("tool2");
        tool2.setName("tool2");

        when(userToolMapper.selectList(any())).thenReturn(List.of(userTool));
        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(roleToolMapper.selectList(any())).thenReturn(List.of(roleTool));
        when(toolMapper.selectBatchIds(anyList())).thenReturn(List.of(tool1, tool2));

        // When
        boolean result = permissionService.hasToolIds(userId, toolNames, requireAll);

        // Then
        assertTrue(result);
    }

    @Test
    void hasTool_NotRequireAllAndHasNoMatchingTool_Name_ReturnsFalse() {
        // Given
        String userId = "user1";
        List<String> toolNames = List.of("tool3", "tool4");
        boolean requireAll = false;

        // Mock user impl (direct and through roles)
        UserTool userTool = new UserTool();
        userTool.setUserId(userId);
        userTool.setToolId("tool1");

        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId("role1");

        RoleTool roleTool = new RoleTool();
        roleTool.setRoleId("role1");
        roleTool.setToolId("tool2");

        Tool tool1 = new Tool();
        tool1.setId("tool1");
        tool1.setName("tool1");

        Tool tool2 = new Tool();
        tool2.setId("tool2");
        tool2.setName("tool2");

        when(userToolMapper.selectList(any())).thenReturn(List.of(userTool));
        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(roleToolMapper.selectList(any())).thenReturn(List.of(roleTool));
        when(toolMapper.selectBatchIds(anyList())).thenReturn(List.of(tool1, tool2));

        // When
        boolean result = permissionService.hasToolIds(userId, toolNames, requireAll);

        // Then
        assertFalse(result);
    }

    @Test
    void getUserRoles_Success() {
        // Given
        String userId = "user1";

        UserRole userRole1 = new UserRole();
        userRole1.setUserId(userId);
        userRole1.setRoleId("role1");

        UserRole userRole2 = new UserRole();
        userRole2.setUserId(userId);
        userRole2.setRoleId("role2");

        Role role1 = new Role();
        role1.setId("role1");
        role1.setName("admin");

        Role role2 = new Role();
        role2.setId("role2");
        role2.setName("user");

        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole1, userRole2));
        when(roleMapper.selectBatchIds(anyList())).thenReturn(List.of(role1, role2));

        // When
        List<String> result = permissionService.getRoleNames(userId);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("admin"));
        assertTrue(result.contains("user"));
        verify(userRoleMapper).selectList(any());
        verify(roleMapper).selectBatchIds(anyList());
    }

    @Test
    void getUserRoles_NoRoles_ReturnsEmptyList() {
        // Given
        String userId = "user1";

        when(userRoleMapper.selectList(any())).thenReturn(List.of());

        // When
        List<String> result = permissionService.getRoleNames(userId);

        // Then
        assertTrue(result.isEmpty());
        verify(userRoleMapper).selectList(any());
    }

    @Test
    void getUserTools_Success() {
        // Given
        String userId = "user1";

        // Direct user impl
        UserTool userTool = new UserTool();
        userTool.setUserId(userId);
        userTool.setToolId("tool1");

        // Role-based impl
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId("role1");

        RoleTool roleTool = new RoleTool();
        roleTool.setRoleId("role1");
        roleTool.setToolId("tool2");

        Tool tool1 = new Tool();
        tool1.setId("tool1");
        tool1.setName("directTool");

        Tool tool2 = new Tool();
        tool2.setId("tool2");
        tool2.setName("roleTool");

        when(userToolMapper.selectList(any())).thenReturn(List.of(userTool));
        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(roleToolMapper.selectList(any())).thenReturn(List.of(roleTool));
        when(toolMapper.selectBatchIds(anyList())).thenReturn(List.of(tool1, tool2));

        // When
        List<String> result = permissionService.getUserTools(userId);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("directTool"));
        assertTrue(result.contains("roleTool"));
        verify(userToolMapper).selectList(any());
        verify(userRoleMapper).selectList(any());
        verify(roleToolMapper).selectList(any());
        verify(toolMapper).selectBatchIds(anyList());
    }

    @Test
    void getUserTools_NoTools_ReturnsEmptyList() {
        // Given
        String userId = "user1";

        when(userToolMapper.selectList(any())).thenReturn(List.of());
        when(userRoleMapper.selectList(any())).thenReturn(List.of());

        // When
        List<String> result = permissionService.getUserTools(userId);

        // Then
        assertTrue(result.isEmpty());
        verify(userToolMapper).selectList(any());
        verify(userRoleMapper).selectList(any());
    }
}